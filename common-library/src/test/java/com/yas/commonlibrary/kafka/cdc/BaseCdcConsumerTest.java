package com.yas.commonlibrary.kafka.cdc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;

class BaseCdcConsumerTest {

    @Test
    void processMessage_withValueOnly_invokesConsumer() {
        TestConsumer consumer = new TestConsumer();
        AtomicReference<String> captured = new AtomicReference<>();
        MessageHeaders headers = new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, "key-1"));

        consumer.processValue("value-1", headers, captured::set);

        assertEquals("value-1", captured.get());
    }

    @Test
    void processMessage_withKeyAndValue_invokesConsumer() {
        TestConsumer consumer = new TestConsumer();
        AtomicReference<String> capturedKey = new AtomicReference<>();
        AtomicReference<String> capturedValue = new AtomicReference<>();
        MessageHeaders headers = new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, "key-2"));

        consumer.processKeyValue("key-2", "value-2", headers, (key, value) -> {
            capturedKey.set(key);
            capturedValue.set(value);
        });

        assertEquals("key-2", capturedKey.get());
        assertEquals("value-2", capturedValue.get());
    }

    static class TestConsumer extends BaseCdcConsumer<String, String> {
        void processValue(String value, MessageHeaders headers, java.util.function.Consumer<String> consumer) {
            processMessage(value, headers, consumer);
        }

        void processKeyValue(String key, String value, MessageHeaders headers, java.util.function.BiConsumer<String, String> consumer) {
            processMessage(key, value, headers, consumer);
        }
    }
}
