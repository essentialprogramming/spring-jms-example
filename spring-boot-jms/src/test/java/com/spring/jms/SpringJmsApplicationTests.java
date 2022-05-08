package com.spring.jms;

import com.spring.jms.messaging.MessageReceiver;
import com.spring.jms.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"activemq-embedded"})
class SpringJmsApplicationTests {

	@Autowired
	private MessageService messageService;

	@Autowired
	private MessageReceiver receiver;

	@Test
	public void testReceive() throws Exception {
		messageService.sendMessage("Hello Spring JMS ActiveMQ!");

		receiver.getCountDownLatch().await(10000, TimeUnit.MILLISECONDS);
		assertThat(receiver.getCountDownLatch().getCount()).isEqualTo(0);
	}

}
