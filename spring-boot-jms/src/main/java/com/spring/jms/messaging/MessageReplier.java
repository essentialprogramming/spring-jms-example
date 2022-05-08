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


    @JmsListener(destination = "${destination.queue.request}", containerFactory = "replyJmsListenerContainerFactory")
    public void onMessage(Message message, Session session) throws JMSException {
        readMessage((TextMessage) message, session);
    }

    @Override
    protected void readMessage(TextMessage message, Session session) throws JMSException {
        final var messageText = getInformation(message, TextMessage::getText).orElse("");
        if (isDeliveryCountExceededOf(message)) {
            reportMessageAsError(messageText, requestQueue);
        } else {
            processMessage(message.getJMSCorrelationID(), messageText, session, message.getJMSReplyTo());
        }
    }

    protected void processMessage(final String messageID, final String message,
                                  final Session session, final Destination replyToDestination) throws JMSException {
        log.info("Received message : {}:{}", messageID, message);

        // Send a reply message
        final MessageProducer replyDestination = session.createProducer(replyToDestination);
        final TextMessage replyMsg = session.createTextMessage("Received: " + message);
        replyMsg.setJMSCorrelationID(messageID);
        replyDestination.send(replyMsg);

        log.info("Sending reply message");
    }
}
