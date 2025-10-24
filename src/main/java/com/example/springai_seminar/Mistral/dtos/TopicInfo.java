package com.example.springai_seminar.Mistral.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicInfo
{
    @JsonProperty("định nghĩa")
    @JsonPropertyDescription("Định nghĩa của chủ đề")
    private String definition;

    @JsonProperty("chức năng")
    @JsonPropertyDescription("Các đặc điểm chính của chủ đề")
    private List<String> mainFeatures;

    @JsonProperty("ví dụ")
    @JsonPropertyDescription("Ví dụ minh họa")
    private String example;

    @JsonProperty("ứng dụng thực tế")
    @JsonPropertyDescription("Ứng dụng thực tế chủ đề")
    private List<String> realWorldApplications;
}
