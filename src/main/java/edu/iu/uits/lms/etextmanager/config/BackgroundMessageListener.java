package edu.iu.uits.lms.etextmanager.config;

import com.rabbitmq.client.Channel;
import edu.iu.uits.lms.etextmanager.service.ETextService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RabbitListener(queues = "${etextmanager.backgroundQueueName}")
@Profile("!batch")
@Component
@Slf4j
public class BackgroundMessageListener {

   @Autowired
   private ETextService eTextService;

   @RabbitHandler //(isDefault = true)
   public void receive(BackgroundMessage message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
      log.info("Received <{}>", message);

      try {
         // ack the message
         channel.basicAck(deliveryTag, false);

         // do the message stuff!
         eTextService.processCsvData(message.getUsername(), message.getFileGroup());
      } catch (IOException e) {
         log.error("unable to ack the message from the queue", e);
      }
   }

}
