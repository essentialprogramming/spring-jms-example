package com.spring.jms.utils.mq;


import lombok.extern.slf4j.Slf4j;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.Optional;

@Slf4j
public abstract class QueueListener {

    private static final int DELIVERY_COUNT_LIMIT = 10;


    protected Optional<String> getInformation(final TextMessage message, final JmsFunction func) {
        try {
            return message != null ? Optional.ofNullable(func.apply(message)) : Optional.empty();
        } catch (final JMSException e) {
            log.error("Can't get jms information {}", message, e);
            return Optional.empty();
        }
    }

    protected boolean isDeliveryCountExceededOf(final TextMessage message) {
        final Optional<Integer> deliveryCount = getDeliveryCountOf(message);
        return deliveryCount.isPresent() && deliveryCount.get() > DELIVERY_COUNT_LIMIT;
    }

    protected void reportMessageAsError(final String message, final String destination) {
        log.warn("Delivery count of message is exceeded. Message can't be processed.");
    }

    private Optional<Integer> getDeliveryCountOf(final TextMessage message) {
        try {
            // https://stackoverflow.com/questions/28020111/when-jmsxdeliverycount-get-increased
            return Optional.of(message.getIntProperty("JMSXDeliveryCount"));
        } catch (final JMSException | NumberFormatException e) {
            log.warn("Can't get delivery count for message.");
            return Optional.empty();
        }
    }

    protected abstract void readMessage(final TextMessage message, final Session session) throws JMSException;

    protected interface JmsFunction {
        String apply(TextMessage message) throws JMSException;
    }

}
