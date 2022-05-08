package com.spring.jms.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.util.ObjectUtils;

import javax.jms.Destination;

@Configuration
@EnableJms
@Slf4j
public class JMSConfig {

    @Value("${destination.queue.hello}")
    private String destination;

    @Value("${destination.queue.request}")
    private String requestQueue;

    @Value("${destination.queue.response}")
    private String responseQueue;

    @Value("${activemq.broker.url}")
    private String brokerUrl;

    @Value( "${activemq.user}" )
    private String user;

    @Value( "${activemq.password}" )
    private String password;

    @Value("${activemq.client.timeout}")
    private int timeout;

    @Bean
    public ActiveMQConnectionFactory activeMQConnectionFactory() {
        log.info("Init ActiveMQ..");

        final ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
        activeMQConnectionFactory.setBrokerURL(brokerUrl);
        if (!ObjectUtils.isEmpty(user)) {
            activeMQConnectionFactory.setUserName(user);
            activeMQConnectionFactory.setPassword(password);
        }

        return activeMQConnectionFactory;
    }


    @Bean
    public CachingConnectionFactory cachingConnectionFactory() {
        return new CachingConnectionFactory(activeMQConnectionFactory());
    }

    @Bean
    public Destination sayHelloDestination() {
        return new ActiveMQQueue(destination);
    }

    @Bean
    public Destination requestQueue() {
        return new ActiveMQQueue(requestQueue);
    }

    @Bean
    public Destination responseQueue() {
        return new ActiveMQQueue(responseQueue);
    }

    @Bean
    public JmsTemplate jmsTemplate() {
        final JmsTemplate jmsTemplate = new JmsTemplate(cachingConnectionFactory());
        jmsTemplate.setReceiveTimeout(timeout);
        jmsTemplate.setDefaultDestination(sayHelloDestination());

        return jmsTemplate;
    }


    @Bean
    public JmsListenerContainerFactory<DefaultMessageListenerContainer> defaultJmsListenerContainerFactory(
            final DefaultJmsListenerContainerFactoryConfigurer configurer) {

        final DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();

        // This provides all boot's default to this factory, including the message converter
        configurer.configure(factory, activeMQConnectionFactory());

        factory.setConcurrency("1-3");
        factory.setClientId("clientID");

        return factory;
    }

    @Bean
    public JmsListenerContainerFactory<DefaultMessageListenerContainer> replyJmsListenerContainerFactory(
            final DefaultJmsListenerContainerFactoryConfigurer configurer) {

        final DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();

        // This provides all boot's default to this factory, including the message converter
        configurer.configure(factory, activeMQConnectionFactory());

        factory.setConcurrency("1-3");
        factory.setClientId("replyClientID");

        return factory;
    }

}
