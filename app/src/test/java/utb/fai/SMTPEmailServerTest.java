package utb.fai;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static org.junit.Assert.assertEquals;

import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import utb.fai.Core.NATTContext;
import utb.fai.Module.SMTPEmailServer;

public class SMTPEmailServerTest {

    private final int port = 2525;
    private SMTPEmailServer emailServer;

    @Before
    public void setUp() throws Exception {
        emailServer = new SMTPEmailServer("Test-Server", port);
        emailServer.runModule();
    }

    @After
    public void tearDown() throws Exception {
        emailServer.terminateModule();
    }

    @Test
    public void testReceiveMessage() throws Exception {
        sendTestEmail("testuser@example.com", "Test Message", "This is a test message");
        Thread.sleep(2000);

        CopyOnWriteArrayList<utb.fai.Core.MessageBuffer.NATTMessage> receivedMessages = NATTContext.instance()
                .getMessageBuffer().getMessages("Test-Server");

        assertEquals(1, receivedMessages.size());
        assertEquals("Test Message", receivedMessages.get(0).getTag());
        assertEquals("This is a test message", receivedMessages.get(0).getMessage());

        NATTContext.instance().getMessageBuffer().clearAll();
    }

    @Test
    public void testReceiveMultipleMessage() throws Exception {
        sendTestEmail("testuser@example.com", "Test Message", "This is a test message");
        sendTestEmail("testuser@example.com", "Test Message", "This is a test message");
        sendTestEmail("testuser@example.com", "Test Message", "This is a test message");
        sendTestEmail("testuser@example.com", "Test Message", "This is a test message");
        Thread.sleep(2000);

        CopyOnWriteArrayList<utb.fai.Core.MessageBuffer.NATTMessage> receivedMessages = NATTContext.instance()
                .getMessageBuffer().getMessages("Test-Server");

        assertEquals(4, receivedMessages.size());

        NATTContext.instance().getMessageBuffer().clearAll();
    }

    private void sendTestEmail(String to, String subject, String content) throws MessagingException {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "localhost");
        properties.put("mail.smtp.port", port + "");

        Session session = Session.getInstance(properties);

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("testsender@example.com"));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject);
        message.setText(content);

        Transport.send(message);
    }

}
