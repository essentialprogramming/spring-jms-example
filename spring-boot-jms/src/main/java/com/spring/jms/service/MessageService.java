package com.spring.jms.service;

import com.spring.jms.messaging.MessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import javax.jms.Destination;
import javax.jms.JMSException;
import java.io.Serializable;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    @Value("${destination.queue.hello}")
    private String destination;

    private final Destination requestQueue;
    private final Destination responseQueue;

    private final MessageSender sender;

    public void sendMessage(final Serializable message) {
        sender.send(destination, (String) message);
    }

    public void ping() {
        sender.ping(destination);
    }

    public String sendAndReceive(final Serializable message) throws JMSException {
        return sender.sendAndReceive(requestQueue, responseQueue, (String) message);
    }
}
