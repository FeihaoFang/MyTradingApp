package com.example.myapp.service;

import com.example.myapp.entity.KlineData;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;

import java.util.List;
@Validated
public abstract class KlineDataSourceService {
    @Autowired
    protected RestTemplate restTemplate;

    protected abstract List<KlineData> parseResponseBody (@NotNull Object body, @NotBlank String symbol);
    protected abstract String buildApiUrl(@NotBlank String symbol, @NotNull long startTime, @NotNull long endTime);
    protected abstract List<String> getAllSymbols();

    public List<KlineData> getData(@NotBlank String symbol, @NotNull @Min(0) Long startTime, @NotNull @Min(0) Long endTime) {
        String url = buildApiUrl(symbol, startTime, endTime);
        ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
        return parseResponseBody(response.getBody(), symbol);
    }


}