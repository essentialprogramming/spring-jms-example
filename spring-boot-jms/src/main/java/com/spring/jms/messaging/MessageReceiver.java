package com.spring.jms.messaging;

import com.spring.jms.utils.mq.QueueListener;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;


@Component
@Slf4j
public class MessageReceiver extends QueueListener {

    @Value("${destination.queue.hello}")
    private String destination;

    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    /**
     * Entrypoint for Messages from the `sayHello` queue.
     * It checks if the message has exceeded the delivery count.
     * When the delivery count is below 10 the message processing is started.
     *
     * @param message Message from the queue
     */
    @JmsListener(destination = "${destination.queue.hello}", containerFactory = "defaultJmsListenerContainerFactory")
    public void receiveMessage(Message message) {
        readMessage((TextMessage) message, null);
    }

    @Override
    protected void readMessage(final TextMessage message, final Session session) {
        final var messageText = getInformation(message, textMessage -> ((TextMessage) textMessage).getText()).orElse("");
        if (isDeliveryCountExceededOf(message)) {
            reportMessageAsError(messageText, destination);
        } else {
            processMessage(messageText);
        }
    }



    protected void processMessage(final String message) {
        log.info("Received message: {}", message);
        messageIsRelevant(message).ifPresent(x -> {
            countDownLatch.countDown();
        });
    }

    private Optional<Boolean> messageIsRelevant(final String messageText) {
        if (ObjectUtils.isEmpty(messageText)) {
            return Optional.empty();
        }
        return Optional.of(true);
    }
}
