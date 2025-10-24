package com.example.springai_seminar;

import com.example.springai_seminar.Mistral.MistralAIService;
import com.example.springai_seminar.Mistral.dtos.ChatRequestDTO;
import com.example.springai_seminar.Mistral.dtos.SearchRequestDTO;
import com.example.springai_seminar.entities.Book;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/")
@Tag(name = "LLM")
@RequiredArgsConstructor
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

    @PostMapping("/generate")
    public ResponseEntity<String> generateBooks(@RequestBody ChatRequestDTO req) {
        _service.generateBooksFromPrompt(req.getPrompt());
        return ResponseEntity.status(HttpStatus.CREATED).body("Ok");
    }

    @GetMapping("/search")
    public List<Map<String, Object>> searchByTitle(@RequestBody SearchRequestDTO req) {
        return _service.searchByPrompt(req);
    }
}