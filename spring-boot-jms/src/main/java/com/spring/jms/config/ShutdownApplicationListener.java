package com.spring.jms.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Component
@RequiredArgsConstructor
@Slf4j
public class ShutdownApplicationListener implements ApplicationListener<ContextClosedEvent> {

    public void onApplicationEvent(@NonNull ContextClosedEvent event) {
        log.info("Application shutting down..");
    }
}