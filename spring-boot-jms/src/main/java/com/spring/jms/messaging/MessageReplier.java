package com.spring.jms.messaging;

import com.spring.jms.utils.mq.QueueListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageReplier extends QueueListener {

    @Value("${destination.queue.request}")
    private String requestQueue;

    /**
     * Entrypoint for Messages from the request queue.
     * It checks if the message has exceeded the delivery count.
     * When the delivery count is below 10 the message processing is started.
     *
     * @param message Message from the queue
     */
    @JmsListener(destination = "${destination.queue.request}", containerFactory = "replyJmsListenerContainerFactory")
    public void onMessage(final Message message, final Session session) throws JMSException {
        if (message instanceof TextMessage) {
            readMessage((TextMessage) message, session);
        }
        if (message instanceof BytesMessage) {
            readMessage((BytesMessage) message, session);
        }
    }

    @Override
    protected void readMessage(final TextMessage message, final Session session) throws JMSException {
        final var messageText = getInformation(message, textMessage -> ((TextMessage) textMessage).getText()).orElse("");
        if (isDeliveryCountExceededOf(message)) {
            reportMessageAsError(messageText, requestQueue);
        } else {
            processMessage(message.getJMSCorrelationID(), messageText, session, message.getJMSReplyTo());
        }
    }

    protected void readMessage(final BytesMessage message, final Session session) throws JMSException {
        final var messageText = getInformation(message, bytesMessage -> ((BytesMessage) bytesMessage).readUTF()).orElse("");
        if (isDeliveryCountExceededOf(message)) {
            reportMessageAsError(messageText, requestQueue);
        } else {
            processMessage(message.getJMSCorrelationID(), messageText, session, message.getJMSReplyTo());
        }
    }

    protected void processMessage(final String correlationID, final String message,
                                  final Session session, final Destination replyToDestination) throws JMSException {
        log.info("Received message with JMSCorrelationID {} : {}", correlationID, message);

        // Send a reply message
        final MessageProducer replyDestination = session.createProducer(replyToDestination);
        final TextMessage replyMsg = handleMessage(message, session);
        replyMsg.setJMSCorrelationID(correlationID);
        replyDestination.send(replyMsg);

        log.info("Sending reply message");
    }

    private TextMessage handleMessage(final String message, final Session session) throws JMSException {
        final TextMessage replyMsg = session.createTextMessage("Received: " + message);
        if (cannotProcessMessage(message)) {
            return handleErrorMessage(message, session);
        }
        return replyMsg;
    }

    private boolean cannotProcessMessage(final String message) {
        return false;
    }

    private TextMessage handleErrorMessage(final String message, final Session session) throws JMSException {
        return session.createTextMessage("Error occurred during processing: " + message);
    }
}
