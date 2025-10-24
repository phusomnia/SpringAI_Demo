package com.example.springai_seminar.Mistral;

import com.example.springai_seminar.Mistral.config.MistralAIConfig;
import com.example.springai_seminar.Mistral.config.VectorStoreConfig;
import com.example.springai_seminar.Mistral.dtos.SearchRequestDTO;
import com.example.springai_seminar.Mistral.dtos.TopicInfo;
import com.example.springai_seminar.Mistral.repos.BookRepository;
import com.example.springai_seminar.Mistral.dtos.ChatRequestDTO;
import com.example.springai_seminar.entities.Book;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MistralAIService {
    private final MistralAIConfig _config;
    private final BookRepository _bookRepo;
    private final ObjectMapper _objectMapper = new ObjectMapper();
    private final VectorStoreConfig _vectorConfig;

    public String chatModel(ChatRequestDTO req)
    {
        return  _config.mistralAiChatModel().call(req.getPrompt());
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
    public void generateBooksFromPrompt(String userPrompt) {
        String prompt = """
        Bạn là hệ thống sinh dữ liệu sách.
        Hãy đọc yêu cầu của người dùng và tạo danh sách sách tương ứng.

        Yêu cầu người dùng:
        %s

        Chỉ trả về JSON với định dạng sau, không có bất kỳ văn bản bổ sung nào. 
        Đảm bảo không có ký tự bổ sung hoặc khối mã markdown:
        [
          {"title": "string", "author": "string", "description": "string"},
          ...
        ]
        """.formatted(userPrompt);

        try {
            String json = _config.mistralChatClient()
                    .prompt(prompt)
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
            
            List<Book> books = _objectMapper.readValue(json, new TypeReference<List<Book>>() {});
            
            books.forEach(book -> book.setId(UUID.randomUUID().toString()));
            
            saveBooksAsVectors(books);
        } catch (Exception e) {
            throw new RuntimeException("Cannot parse AI response", e);
        }
    }

    public void saveBooksAsVectors(List<Book> books) {
        List<Document> docs = books.stream()
                .map(Book::toDocument)
                .toList();
        _vectorConfig.vectorStore().add(docs);
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

    private Map<String,Object> documentToMap(Document document) {
        Map<String,Object> map = new HashMap<>(document.getMetadata());
        map.put("content", document.getFormattedContent());
        map.put("score", document.getScore());
        return map;
    }
}
