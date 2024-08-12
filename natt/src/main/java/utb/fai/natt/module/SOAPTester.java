package utb.fai.natt.module;

import utb.fai.natt.spi.NATTModule;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import utb.fai.natt.core.NATTContext;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.NATTLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * Tento modul obsahuje implementaci testovaciho klienta pro SOAP webovou
 * sluzbu. Umoznuje odesilani pozadavku a prijimani odpovedi.
 * 
 * Prijate zpravy jsou do message bufferu ukladany v textove podobne prevedene
 * do json formatu. Tag je vzdy prazdny ""
 */
@NATTAnnotation.Module("soap-tester")
public class SOAPTester extends NATTModule {

    /**
     * Prefix ktery je do message bufferu ulozen spolu s obdrzenim error status
     * kodem. Format: ERROR: <error-code>
     */
    public static final String ERROR_CODE_PREFIX = "ERROR: ";

    private NATTLogger logger = new NATTLogger(SOAPTester.class);

    protected String url;

    protected CloseableHttpClient httpClient;

    public SOAPTester(String name, String url) throws NonUniqueModuleNamesException, InternalErrorException {
        super(name, NATTContext.instance());

        if (url == null) {
            throw new InternalErrorException("Service URL is null");
        }
        if (url.isEmpty()) {
            throw new InternalErrorException("Service URL is empty");
        }

        this.url = url;
    }

    @Override
    public void runModule() throws InternalErrorException {
        // vytvori http klienta
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .setSocketTimeout(5000)
                .build();
        this.httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
        super.setRunning(true);
    }

    @Override
    public boolean terminateModule() {
        // odstraneni tohoto modulu z aktivnich modulu
        super.setRunning(false);
        try {
            this.httpClient.close();
        } catch (IOException e) {
            logger.warning(super.getNameForLogger() + "Failed to close client: " + e.getMessage());
            return false;
        }

        logger.info(super.getNameForLogger() + String.format("SOAP tester [%s] terminated", this.getName()));
        return NATTContext.instance().removeActiveModule(this.getName());
    }

    @Override
    public boolean sendMessage(String message) throws InternalErrorException {
        if (message == null || message.isEmpty()) {
            return false;
        }
    
        // vytvoreni pozadavku
        HttpPost httpPost = new HttpPost(url);
    
        // nastavi SOAP XML jako obsahu pozadavku
        StringEntity entity = new StringEntity(message, ContentType.create("text/xml", "UTF-8"));
        httpPost.setEntity(entity);
    
        // odeslani pozadavku na SOAP sluzbu
        logger.info(super.getNameForLogger() + "Sending a request on URL: " + this.url);
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {

            // zpracovani odpovedi
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode >= 400) {
                    logger.warning(super.getNameForLogger() + "HTTP request returned an error status: " + statusCode);
                    NATTContext.instance().getMessageBuffer().addMessage(getName(), "",
                            SOAPTester.ERROR_CODE_PREFIX + String.valueOf(statusCode));
                    super.notifyMessageListeners("", RESTTester.ERROR_CODE_PREFIX + String.valueOf(statusCode));
                    return true;
                }
    
                try (BufferedReader rd = new BufferedReader(new InputStreamReader(responseEntity.getContent()))) {
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = rd.readLine()) != null) {
                        result.append(line);
                    }
                    String jsonResponse = convertXmlToJson(result.toString(), "Body");
                    NATTContext.instance().getMessageBuffer().addMessage(this.getName(), "", jsonResponse);
                    super.notifyMessageListeners("", jsonResponse);
                }
            }
            
        } catch (IOException e) {
            logger.warning(super.getNameForLogger() + "Failed to send request: " + e.getMessage());
            return false;
        } catch (Exception e) {
            logger.warning(super.getNameForLogger() + "Failed process response: " + e.getMessage());
            return false;
        }
    
        return true;
    }

    private String convertXmlToJson(String xml, String taget) throws Exception {
        ObjectMapper xmlMapper = new XmlMapper();
        JsonNode rootNode = xmlMapper.readTree(xml.getBytes());
        JsonNode bodyNode = rootNode.get(taget);
        ObjectMapper jsonMapper = new ObjectMapper();
        return jsonMapper.writeValueAsString(bodyNode);
    }

}
