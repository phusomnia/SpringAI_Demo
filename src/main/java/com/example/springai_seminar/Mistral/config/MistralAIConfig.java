package com.example.springai_seminar.Mistral.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mistralai.MistralAiChatModel;
import org.springframework.ai.mistralai.MistralAiChatOptions;
import org.springframework.ai.mistralai.MistralAiEmbeddingModel;
import org.springframework.ai.mistralai.MistralAiEmbeddingOptions;
import org.springframework.ai.mistralai.api.MistralAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MistralAIConfig {
    @Value("${spring.ai.mistralai.api-key}")
    private String apiKey;

    @Value("${spring.ai.mistralai.chat.options.model}")
    private String chatModel;

    @Value("${spring.ai.mistralai.chat.options.temperature}")
    private Double temperature;

    @Value("${spring.ai.mistralai.chat.options.max-tokens}")
    private Integer maxTokens;

    @Value("${spring.ai.mistralai.embedding.options.model}")
    private String embeddingModel;

    @Value("${spring.ai.mistralai.embedding.options.encoding-format}")
    private String encodingFormat;

    // --- Chat ---

    @Bean
    public MistralAiApi mistralAiApi()
    {
        return new MistralAiApi(apiKey);
    }

    @Bean
    public MistralAiChatOptions mistralAiChatOptions()
    {
        return MistralAiChatOptions.builder()
                .model(chatModel)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();
    }

    @Bean
    public MistralAiChatModel mistralAiChatModel() {
        return MistralAiChatModel.builder()
                .mistralAiApi(this.mistralAiApi())
                .defaultOptions(this.mistralAiChatOptions())
                .build();
    }

    @Bean
    public ChatClient mistralChatClient() {
        return ChatClient
                .builder(this.mistralAiChatModel())
                .build();
    }

    // --- Embedding ---
    @Bean
    public MistralAiEmbeddingOptions mistralAiEmbeddingOptions() {
        return MistralAiEmbeddingOptions.builder()
                .withModel(embeddingModel)
                .withEncodingFormat(encodingFormat)
                .build();
    }
    
    @Bean
    public MistralAiEmbeddingModel embeddingModel()
    {

        return new MistralAiEmbeddingModel(mistralAiApi(), mistralAiEmbeddingOptions());
    }
}
