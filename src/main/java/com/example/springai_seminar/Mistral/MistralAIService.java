package com.example.springai_seminar.Mistral;

import com.example.springai_seminar.Mistral.config.MistralAIConfig;
import com.example.springai_seminar.Mistral.config.VectorStoreConfig;
import com.example.springai_seminar.Mistral.dtos.SearchRequestDTO;
import com.example.springai_seminar.Mistral.dtos.TopicInfo;
import com.example.springai_seminar.Mistral.dtos.ChatRequestDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MistralAIService {
    private final MistralAIConfig _config;
    private final ObjectMapper _objectMapper = new ObjectMapper();
    private final VectorStoreConfig _vectorConfig;

    public String chatModel(ChatRequestDTO req)
    {
        log.info("Starting async task on thread: {}", Thread.currentThread().getName());
        String result = _config.mistralChatClient().prompt(req.getPrompt()).call().content();
        log.info("Completed task on thread: {}", Thread.currentThread().getName());
        return result;
    }
    
    @Async("taskExecutor")
    public CompletableFuture<String> chatModelAsync(ChatRequestDTO req)
    {
        log.info("Starting async task on thread: {}", Thread.currentThread().getName());
        String result = _config.mistralChatClient().prompt(req.getPrompt()).call().content();
        log.info("Completed task on thread: {}", Thread.currentThread().getName());
        return CompletableFuture.completedFuture(result);
    }

    public Flux<String> chatModelFlux(ChatRequestDTO req) {
        return _config.mistralChatClient().prompt(req.getPrompt()).stream().content();
    }
    
    /* STRUCTED OUTPUT */
    public TopicInfo chatStructedOutput(ChatRequestDTO req)
    {
        BeanOutputConverter<TopicInfo> outputConverter = new BeanOutputConverter<>(TopicInfo.class);

        PromptTemplate promptTemplate = new PromptTemplate("""
            Hãy cung cấp thông tin chi tiết về: {topic}
            
            Yêu cầu:
            1. Định nghĩa rõ ràng
            2. Các đặc điểm chính
            3. Ví dụ minh họa
            4. Ứng dụng thực tế

            {format}
        """);

        promptTemplate.add("topic", req.toString());
        promptTemplate.add("format", outputConverter.getFormat());

        var result = _config.mistralChatClient().prompt(promptTemplate.render()).call().content();
        return outputConverter.convert(result);
    }
    
    /* GENERATE DATA WITH PROMPT */
    public List<Map<String, Object>> generateDataFromPrompt(String userPrompt) {
        try {
            String json = _config.mistralChatClient()
                    .prompt(userPrompt)
                    .call()
                    .content();
            
            if (json.startsWith("```json")) {
                json = json.substring(7);
            }
            if (json.startsWith("```")) {
                json = json.substring(3);
            }
            if (json.endsWith("```")) {
                json = json.substring(0, json.length() - 3);
            }
            json = json.trim();

            List<Map<String, Object>> result = _objectMapper.readValue(json, new TypeReference<>(){});
            
            this.saveAsVectors(result);
            
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Cannot parse AI response", e);
        }
    }
    
    public void saveAsVectors(List<Map<String, Object>> data)
    {
        List<Document> documents = data.stream()
                .map(item -> {
                    String content = item.getOrDefault("content", "").toString();
                    
                    Map<String, Object> metadata = new HashMap<>();
                    for (Map.Entry<String, Object> entry : item.entrySet()) {
                        if (!"content".equals(entry.getKey())) {
                            metadata.put(entry.getKey(), entry.getValue());
                        }
                    }

                    return new Document(content, metadata);
                }).collect(Collectors.toList());
        
        _vectorConfig.vectorStore().add(documents);
    }
    
    public List<Map<String, Object>> searchByPrompt(SearchRequestDTO req) {
        SearchRequest request = SearchRequest.builder()
                .query(req.getQuery()) 
                .topK(Math.max(1, req.getTopK()))
                .build();
        
        return _vectorConfig.vectorStore()
                .similaritySearch(request)
                .stream()
                .limit(req.getTopK())
                .map(this::documentToMap)
                .collect(Collectors.toList());
    }

    private Map<String, Object> documentToMap(Document doc) {
        Map<String, Object> map = new HashMap<>();
        if (doc.isText()) {
            map.put("content", doc.getText());
        } else {
            map.put("media", doc.getMedia());
        }

        Map<String, Object> metadata = doc.getMetadata();
        System.out.println("DEBUG - Full metadata: " + metadata);
        if (!metadata.isEmpty()) {
            map.putAll(metadata);
        }
        
        map.put("id", doc.getId());
        Double score = doc.getScore();
        if (score != null) {
            map.put("score", score);
        }

        return map;
    }
}
