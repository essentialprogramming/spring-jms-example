package com.spring.jms.messaging;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.jms.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageSender {

    private final JmsTemplate jmsTemplate;

    public void send(final String queueName, final String message) {
        log.info("Sending message `{}` to destination queue `{}` ", message, queueName);
        jmsTemplate.convertAndSend(queueName, message);
    }

    public void ping(final String queueName) {
        final MessageCreator messageCreator = (session)
                -> session.createTextMessage("ping..");

        log.info("Pinging destination `{}`", queueName);
        jmsTemplate.send(queueName, messageCreator);

    }

    public String sendAndReceive(final Destination requestQueue, final Destination replyToDestination, final String message) throws JMSException {

        final String replyMessage;
        Message response = jmsTemplate.sendAndReceive(requestQueue, session -> {
            TextMessage textMessage = session.createTextMessage(message);
            textMessage.setJMSCorrelationID(NanoIdUtils.randomNanoId());
            textMessage.setJMSReplyTo(replyToDestination);
            textMessage.setJMSType("REQUEST_REPLY");
            return textMessage;
        });

        if (messageIsRelevant(response)) {
            replyMessage = ((TextMessage) response).getText();
        } else {
            replyMessage = null;
        }

        return replyMessage;
    }

    public byte[] sendAndReceive(final Destination requestQueue, final Destination replyToDestination, final byte[] message) throws
            JMSException {

        final byte[] replyMessage;
        Message response = jmsTemplate.sendAndReceive(requestQueue, session -> {
            BytesMessage bytesMessage = session.createBytesMessage();
            bytesMessage.writeBytes(message);
            bytesMessage.setJMSCorrelationID(NanoIdUtils.randomNanoId());
            bytesMessage.setJMSReplyTo(replyToDestination);
            bytesMessage.setJMSType("REQUEST_REPLY");
            return bytesMessage;
        });

        if (messageIsRelevant(response)) {
            replyMessage = extractByteArrayFromMessage((BytesMessage) response);
        } else {
            replyMessage = new byte[0];
        }

        return replyMessage;
    }

    private boolean messageIsRelevant(final Message message) {
        return !ObjectUtils.isEmpty(message);
    }

    /**
     * Extract a byte array from the given {@link BytesMessage}.
     * @param message the message to convert
     * @return the resulting byte array
     * @throws JMSException if thrown by JMS methods
     */
    private byte[] extractByteArrayFromMessage(final BytesMessage message) throws JMSException {
        final byte[] bytes = new byte[(int) message.getBodyLength()];
        message.readBytes(bytes);

        return bytes;
    }
}