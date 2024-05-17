package utb.fai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.Test;

import utb.fai.Core.MessageBuffer;

public class MessageBufferTest {

    @Test
    public void testAddMessage() {
        MessageBuffer buffer = new MessageBuffer();
        buffer.createMessageBufferForModule("ModuleA");
        boolean result = buffer.addMessage("ModuleA", "TagA", "Hello World!");
        assertTrue(result);
    }

    @Test
    public void testClear() {
        MessageBuffer buffer = new MessageBuffer();
        buffer.createMessageBufferForModule("ModuleA");
        buffer.addMessage("ModuleA", "TagA", "Hello World!");
        buffer.clearAll();
        CopyOnWriteArrayList<MessageBuffer.NATTMessage> messages = buffer.getMessages("ModuleA");
        assertTrue(messages.isEmpty());
    }

    @Test
    public void testRemoveMessage() {
        MessageBuffer buffer = new MessageBuffer();
        buffer.createMessageBufferForModule("ModuleA");
        buffer.addMessage("ModuleA", "TagA", "Hello World!");
        buffer.clearOneMessageBuffer("ModuleA");
        CopyOnWriteArrayList<MessageBuffer.NATTMessage> messages = buffer.getMessages("ModuleA");
        assertTrue(messages.isEmpty());
    }

    @Test
    public void testSearchMessages() {
        MessageBuffer buffer = new MessageBuffer();
        buffer.createMessageBufferForModule("ModuleA");
        buffer.createMessageBufferForModule("ModuleB");
        buffer.addMessage("ModuleA", "TagA", "Hello World!");
        buffer.addMessage("ModuleB", "TagB", "Goodbye World!");
        LinkedList<MessageBuffer.NATTMessage> result = buffer.searchMessages("ModuleA", "TagA", "Hello",
                MessageBuffer.SearchType.CONTAINS, false);
        assertEquals(1, result.size());
        assertEquals("Hello World!", result.getFirst().getMessage());
    }

    @Test
    public void testRemoveMessageFromModule() {
        MessageBuffer buffer = new MessageBuffer();
        buffer.createMessageBufferForModule("ModuleA");
        buffer.addMessage("ModuleA", "TagA", "Hello World!");
        CopyOnWriteArrayList<MessageBuffer.NATTMessage> messagesBefore = buffer.getMessages("ModuleA");
        assertFalse(messagesBefore.isEmpty());

        MessageBuffer.NATTMessage messageToRemove = messagesBefore.get(0);
        buffer.removeMessageFromModule("ModuleA", messageToRemove);
        CopyOnWriteArrayList<MessageBuffer.NATTMessage> messagesAfter = buffer.getMessages("ModuleA");
        assertTrue(messagesAfter.isEmpty());
    }

    @Test
    public void testContainsMessage() {
        MessageBuffer buffer = new MessageBuffer();
        buffer.createMessageBufferForModule("ModuleA");

        assertFalse(buffer.containsMessage("ModuleA"));

        buffer.addMessage("ModuleA", "TagA", "Hello World!");
        assertTrue(buffer.containsMessage("ModuleA"));
    }

    @Test
    public void testSearchMessagesEmptyName() {
        MessageBuffer buffer = new MessageBuffer();
        buffer.createMessageBufferForModule("ModuleA");
        buffer.createMessageBufferForModule("ModuleB");
        buffer.addMessage("ModuleA", "TagA", "Hello World!");
        buffer.addMessage("ModuleB", "TagB", "Goodbye World!");

        // hledani bez specifikace jména modulu
        LinkedList<MessageBuffer.NATTMessage> result = buffer.searchMessages("", "TagA", "Hello",
                MessageBuffer.SearchType.CONTAINS, false);
        assertEquals(1, result.size());
        assertEquals("Hello World!", result.getFirst().getMessage());
    }

    @Test
    public void testSearchMessagesEmptyTag() {
        MessageBuffer buffer = new MessageBuffer();
        buffer.createMessageBufferForModule("ModuleA");
        buffer.createMessageBufferForModule("ModuleB");
        buffer.addMessage("ModuleA", "TagA", "Hello World!");
        buffer.addMessage("ModuleB", "TagB", "Goodbye World!");

        // hledani bez specifikace tagu
        LinkedList<MessageBuffer.NATTMessage> result = buffer.searchMessages("ModuleA", "", "Hello",
                MessageBuffer.SearchType.CONTAINS, false);
        assertEquals(1, result.size());
        assertEquals("Hello World!", result.getFirst().getMessage());
    }

    @Test
    public void testSearchMessagesEmptySearchText() {
        MessageBuffer buffer = new MessageBuffer();
        buffer.createMessageBufferForModule("ModuleA");
        buffer.createMessageBufferForModule("ModuleB");
        buffer.addMessage("ModuleA", "TagA", "Hello World!");
        buffer.addMessage("ModuleB", "TagB", "Goodbye World!");

        // hledani bez specifikace hledaného textu
        LinkedList<MessageBuffer.NATTMessage> result = buffer.searchMessages("ModuleA", "TagA", "",
                MessageBuffer.SearchType.CONTAINS, false);
        assertEquals(1, result.size());
        assertEquals("Hello World!", result.getFirst().getMessage());
    }

    @Test
    public void testSearchMessagesEmpty() {
        MessageBuffer buffer = new MessageBuffer();
        buffer.createMessageBufferForModule("ModuleA");
        buffer.createMessageBufferForModule("ModuleB");
        buffer.addMessage("ModuleA", "TagA", "Hello World!");
        buffer.addMessage("ModuleB", "TagB", "Goodbye World!");

        // hledani bez specifikaci
        LinkedList<MessageBuffer.NATTMessage> result = buffer.searchMessages("", "", "", MessageBuffer.SearchType.NONE,
                false);
        assertEquals(2, result.size());
        assertEquals("Hello World!", result.getFirst().getMessage());
    }
}
