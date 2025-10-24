package com.example.springai_seminar.Mistral.dtos;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class SearchRequestDTO {
    private String query;
    private int topK;
}
