package com.example.springai_seminar.Mistral.config;

import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

@Configuration
public class VectorStoreConfig
{
    @Value("${spring.data.redis.host}")
    private String REDIS_HOST;

    @Value("${spring.data.redis.port}")
    private int REDIS_PORT;

    @Autowired
    private MistralAIConfig _config;

    @Bean
    public JedisPooled jedisPooled() {
        return new JedisPooled(REDIS_HOST, REDIS_PORT);
    }

    @Bean
    public RedisVectorStore vectorStore() {
        return RedisVectorStore.builder(jedisPooled(), _config.embeddingModel())
                .indexName("book-index")
                .prefix("book:")
                .initializeSchema(true)
                .embeddingFieldName("embedding")
                .metadataFields(
                        RedisVectorStore.MetadataField.text("title"),
                        RedisVectorStore.MetadataField.text("description"),
                        RedisVectorStore.MetadataField.text("author")
                )
                .build();
    }
}