package com.spring.jms.messaging;

import com.spring.jms.model.MessageType;
import com.spring.jms.utils.mq.QueueListener;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.*;
import java.nio.charset.StandardCharsets;

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
            processMessage(MessageType.TEXT, messageText, session, message.getJMSCorrelationID(), message.getJMSReplyTo());
        }
    }

    protected void readMessage(final BytesMessage message, final Session session) throws JMSException {
        final var messageText = getInformation(message, bytesMessage -> readMessageAsString((BytesMessage) bytesMessage)).orElse("");
        if (isDeliveryCountExceededOf(message)) {
            reportMessageAsError(messageText, requestQueue);
        } else {
            processMessage(MessageType.BYTES, messageText, session, message.getJMSCorrelationID(), message.getJMSReplyTo());
        }
    }

    protected void processMessage(
            final MessageType messageType, final String message, final Session session,
            final String correlationID, final Destination replyToDestination) throws JMSException {
        log.info("Received message with JMSCorrelationID {} : {}", correlationID, message);

        // Send a reply message
        final MessageProducer replyDestination = session.createProducer(replyToDestination);
        final Message replyMsg = handleMessage(messageType, message, session);
        replyMsg.setJMSCorrelationID(correlationID);
        replyDestination.send(replyMsg);

        log.info("Sending reply message");
    }

    private Message handleMessage(final MessageType messageType, final String message, final Session session) throws JMSException {
        if (cannotProcessMessage(message)) {
            return handleErrorMessage(messageType, message, session);
        }

        return response(messageType, "Received: " + message, session);
    }

    private boolean cannotProcessMessage(final String message) {
        return false;
    }

    private Message handleErrorMessage(final MessageType messageType, final String message, final Session session) throws JMSException {
        return response(messageType, "Error occurred during processing: " + message, session);
    }

    private static Message response(
            final MessageType messageType,
            final String message,
            final Session session) throws JMSException
    {
        switch (messageType) {
            case TEXT:
                return session.createTextMessage(message);
            case BYTES:
                final BytesMessage bytesMessage = session.createBytesMessage();
                bytesMessage.writeBytes(message.getBytes(StandardCharsets.UTF_8));

                return bytesMessage;
            default:
                throw new IllegalArgumentException(); // Can never happen;
        }
    }

    @SneakyThrows
    private static String readMessageAsString(final BytesMessage message) {
        message.reset(); //Puts the message body in read-only mode and repositions the stream of bytes to the beginning

        int messageLength = ((int) message.getBodyLength());
        byte[] messageBytes = new byte[messageLength];
        message.readBytes(messageBytes, messageLength);

        return new String(messageBytes, StandardCharsets.UTF_8).trim();
    }
}
