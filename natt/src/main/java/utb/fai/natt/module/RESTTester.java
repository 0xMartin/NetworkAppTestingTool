package utb.fai.natt.module;

import utb.fai.natt.spi.NATTModule;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import utb.fai.natt.core.NATTContext;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.NATTLogger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Modul obsahuje implementaci pro testovani REST API. Umoznuje odesilat
 * pozdavky typu GET, POST, PUT, DELETE a prijimat zpravy od REST API.
 * 
 * Prijate zpravy jsou do message bufferu ukladany v textove podobne tak jak
 * prisli z testovaneho REST API (json). Pokud dojde k chybe na strane serveru
 * je do message buferu vlozen chybovy status kod. Tag je nastaven ne adresu
 * endpointu, ze ktereho zprava prisla jako odpoved na pozadavek.
 */
@NATTAnnotation.Module("rest-tester")
public class RESTTester extends NATTModule {

    /**
     * Prefix ktery je do message bufferu ulozen spolu s obdrzenim error status
     * kodem. Format: ERROR: <error-code>
     */
    public static final String ERROR_CODE_PREFIX = "ERROR: ";

    private NATTLogger logger = new NATTLogger(RESTTester.class);

    protected String endpoint;
    protected String requestType;
    protected String contentType;

    protected CloseableHttpClient httpClient;

    /**
     * Vytovori instacni REST testeru.
     * 
     * @param name        Nazev modulu
     * @param endpoint    Adresa endpointu
     * @param requestType Typ pozadavku, ktery tester bude odesilat: "get", "post",
     *                    "put", "delete"
     * @param contentType Typ obsahu predavaneho v requestu. Defaultni hodnota:
     *                    application/json
     * @throws NonUniqueModuleNamesException
     */
    public RESTTester(String name, String endpoint, String requestType, String contentType)
            throws NonUniqueModuleNamesException, InternalErrorException {
        super(name, NATTContext.instance());

        if (endpoint == null) {
            throw new InternalErrorException("Endpoint URL is null");
        }
        if (requestType == null) {
            throw new InternalErrorException("Request type is null");
        }
        if (endpoint.isEmpty()) {
            throw new InternalErrorException("Endpoint URL is empty");
        }
        if (requestType.isEmpty()) {
            throw new InternalErrorException("Request type is empty");
        }

        this.endpoint = endpoint;
        this.requestType = requestType;
        if (contentType == null) {
            contentType = "application/json";
        }
        this.contentType = contentType;
    }

    @Override
    public void runModule() throws InternalErrorException {
        // vytvori http klienta
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .setSocketTimeout(5000)
                .build();
        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
        super.setRunning(true);
    }

    @Override
    public boolean terminateModule() {
        super.setRunning(false);

        try {
            this.httpClient.close();
            logger.info(super.getNameForLogger() + String.format("REST tester [%s] terminated", this.getName()));
        } catch (IOException e) {
            logger.warning(super.getNameForLogger() + "Failed to close client: " + e.getMessage());
        }

        return this.getContext().removeActiveModule(this.getName());
    }

    @Override
    public boolean sendMessage(String message) throws InternalErrorException {
        if (message == null) {
            return false;
        }

        // rozdeleni zpravy na casti
        // format "message": <param_name>=<param_value>;...
        // pokud nazev parametru zacina symbolem # tak budou data ulozena do body
        String[] params = null;
        if (!message.isEmpty()) {
            params = message.split(";");
        }

        // vytvori pozadavek
        HttpUriRequest request = null;
        switch (this.requestType.toLowerCase()) {
            case "get":
                request = buildGetRequest(this.endpoint, params);
                break;
            case "post":
                try {
                    request = buildPostRequest(this.endpoint, params);
                } catch (UnsupportedEncodingException e) {
                    logger.warning(super.getNameForLogger() + "Failed to build request: " + e.getMessage());
                    return false;
                }
                break;
            case "put":
                try {
                    request = buildPutRequest(this.endpoint, params);
                } catch (UnsupportedEncodingException e) {
                    logger.warning(super.getNameForLogger() + "Failed to build request: " + e.getMessage());
                    return false;
                }
                break;
            case "delete":
                request = buildDeleteRequest(this.endpoint, params);
                break;
            default:
                throw new InternalErrorException("Invalid type of request: " + this.requestType);
        }

        // odeslani http pozadavku a ziskani odpovedi
        logger.info(super.getNameForLogger() + "Sending a " + this.requestType + " request on endpoint: "
                + request.getURI().toString());
        try (CloseableHttpResponse response = httpClient.execute(request)) {

            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return false;
            } else {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode >= 400) {
                    logger.warning(super.getNameForLogger() + "HTTP request returned an error status: " + statusCode);
                    NATTContext.instance().getMessageBuffer().addMessage(getName(), this.endpoint,
                            RESTTester.ERROR_CODE_PREFIX + String.valueOf(statusCode));
                    super.notifyMessageListeners(this.endpoint,
                            RESTTester.ERROR_CODE_PREFIX + String.valueOf(statusCode));
                    return true;
                }
            }

            // zapis zpravy do bufferu
            String responseBody = EntityUtils.toString(entity);
            NATTContext.instance().getMessageBuffer().addMessage(getName(), this.endpoint, responseBody);
            super.notifyMessageListeners(this.endpoint, responseBody);
        } catch (IOException e) {
            logger.warning(super.getNameForLogger() + "Failed to send or process message: " + e.getMessage());
            return false;
        }

        return true;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    /**
     * Vygeneruje get pozadavek
     * 
     * @param endpoint URL adresa enpointu
     * @param params   Predane parametry
     * @return HttpGet
     */
    public HttpGet buildGetRequest(String endpoint, String[] params) {
        String uri = endpoint;

        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                String[] paramPair = params[i].split("=");
                if (paramPair.length < 2)
                    continue;
                String paramName = paramPair[0];
                String paramValue = paramPair[1];
                if (uri.equals(endpoint)) {
                    uri += "?";
                } else {
                    uri += "&";
                }
                uri += paramName + "=" + paramValue;
            }
        }

        HttpGet request = new HttpGet(uri);
        return request;
    }

    /**
     * Vygeneruje post pozadavek
     * 
     * @param endpoint URL adresa enpointu
     * @param params   Predane parametry
     * @return HttpPost
     */
    public HttpPost buildPostRequest(String endpoint, String[] params) throws UnsupportedEncodingException {
        HttpPost request = new HttpPost(endpoint);
        request.setHeader("Content-Type", this.contentType);

        String uri = endpoint;

        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                String[] paramPair = params[i].split("=");
                if (paramPair.length < 2)
                    continue;
                String paramName = paramPair[0];
                String paramValue = paramPair[1];
                if (paramName.startsWith("#")) {
                    // pokud se jedna o obsah body (nazev parametru zacina '#')
                    request.setEntity(new StringEntity(paramValue));
                } else {
                    // jinak prida jako parametr
                    if (uri.equals(endpoint)) {
                        uri += "?";
                    } else {
                        uri += "&";
                    }
                    uri += paramName + "=" + paramValue;
                }
            }
            request.setURI(java.net.URI.create(uri));
        }

        return request;
    }

    /**
     * Vygeneruje put pozadavek
     * 
     * @param endpoint URL adresa enpointu
     * @param params   Predane parametry
     * @return HttpPut
     */
    public HttpPut buildPutRequest(String endpoint, String[] params) throws UnsupportedEncodingException {
        HttpPut request = new HttpPut(endpoint);
        request.setHeader("Content-Type", this.contentType);

        String uri = endpoint;

        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                String[] paramPair = params[i].split("=");
                if (paramPair.length < 2)
                    continue;
                String paramName = paramPair[0];
                String paramValue = paramPair[1];
                if (paramName.startsWith("#")) {
                    // pokud se jedna o obsah body (nazev parametru zacina '#')
                    request.setEntity(new StringEntity(paramValue));
                } else {
                    // jinak prida jako parametr
                    if (uri.equals(endpoint)) {
                        uri += "?";
                    } else {
                        uri += "&";
                    }
                    uri += paramName + "=" + paramValue;
                }
            }
            request.setURI(java.net.URI.create(uri));
        }

        return request;
    }

    /**
     * Vygeneruje delete pozadavek
     * 
     * @param endpoint URL adresa enpointu
     * @param params   Predane parametry
     * @return HttpDelete
     */
    public HttpDelete buildDeleteRequest(String endpoint, String[] params) {
        String uri = endpoint;

        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                String[] paramPair = params[i].split("=");
                if (paramPair.length < 2)
                    continue;
                String paramName = paramPair[0];
                String paramValue = paramPair[1];
                if (uri.equals(endpoint)) {
                    uri += "?";
                } else {
                    uri += "&";
                }
                uri += paramName + "=" + paramValue;
            }
        }

        HttpDelete request = new HttpDelete(uri);
        return request;
    }

}
