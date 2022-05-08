package com.spring.jms.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.broker.BrokerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConditionalOnProperty(name = ActiveMQServerConfig.ACTIVATION_PROPERTY, havingValue = "true", matchIfMissing = false)
@Slf4j
public class ActiveMQServerConfig {

    static final String ACTIVATION_PROPERTY = "spring.activemq.server.active";

    @Value("${spring.activemq.broker.url}")
    private String brokerUrl;

    /**
     * This represents an embedded JMS server.
     * In production, you always connect to external JMS servers.
     *
     */
    @Bean
    public BrokerService broker() throws Exception {
        log.info("Init Broker..");

        final BrokerService broker = new BrokerService();
        broker.addConnector(brokerUrl);
        broker.setPersistent(false);
        broker.setUseShutdownHook(false);

        return broker;
    }
}
