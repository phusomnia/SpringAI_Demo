package com.example.springai_seminar;

import com.example.springai_seminar.Mistral.MistralAIService;
import com.example.springai_seminar.Mistral.dtos.ChatRequestDTO;
import com.example.springai_seminar.Mistral.dtos.SearchRequestDTO;
//import com.example.springai_seminar.entities.Book;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.springframework.data.redis.connection.ReactiveStreamCommands.AddStreamRecord.body;

@RestController
@RequestMapping("/api/v1/")
@Tag(name = "LLM")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AIController {
    private final MistralAIService _service;
    
    @PostMapping("prompt")
    public ResponseEntity<Object> prompt(@RequestBody ChatRequestDTO req) {
        var resultModel = _service.chatModel(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "prompt", req.getPrompt(),
                "result", resultModel
        ));
    }

    @PostMapping("promptAsync")
    public CompletableFuture<ResponseEntity<Object>> promptAsync(@RequestBody ChatRequestDTO req) {
        var resultModel = _service.chatModelAsync(req);
        return resultModel.thenApply(result -> 
            ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "prompt", req.getPrompt(),
                "result", result
            ))
        );
    }
    
    @PostMapping("chunk")
    public Flux<ResponseEntity<Map<String, Object>>> promptMono(@RequestBody ChatRequestDTO req) {
        var resultModel = _service.chatModelFlux(req);
        return resultModel
                .map(result -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(Map.of(
                                "prompt", req.getPrompt(), 
                                "result", result)
                        )
                );
    }

    @PostMapping("/generate")
    public ResponseEntity<Object> generateBooks(@RequestBody ChatRequestDTO req) {
        var result = _service.generateDataFromPrompt(req.getPrompt());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "prompt", req.getPrompt(),
                "result", result
        ));
    }

    @PostMapping("/search")
    public ResponseEntity<Object> searchByTitle(@RequestBody SearchRequestDTO req) {
        var result =  _service.searchByPrompt(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "prompt", req.getQuery(),
                "result", result
        ));
    }
}