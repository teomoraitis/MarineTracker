package com.di.marinetracker.backendspringboot.configurations;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.util.HashMap;
import java.util.Map;

// Enables Kafka support in Spring Boot
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    // Injects the Kafka bootstrap server address from application properties
    // This is the address+port where the Kafka broker is running
    @Value("${kafka.bootstrapserver}")
    public String bootstrapServer;

    // Defines Kafka consumer configuration properties
    @Bean
    Map<String, Object> consumerConfigs(){
        Map<String, Object> props = new HashMap<>();
        // Kafka broker address:
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        // Consumer group ID:
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "ships-consumer");
        // Start reading from the latest offset:
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        return props;
    }

    // Creates a ConsumerFactory with the above configuration
    // This factory is responsible for creating Kafka consumer instances
    @Bean
    public ConsumerFactory<String, String> consumerFactory(){
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    // Configures the Kafka listener container factory for concurrent message processing
    // This factory is used to create listeners that can process messages concurrently
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String,String>> kafkaListenerContainerFactory(){
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
