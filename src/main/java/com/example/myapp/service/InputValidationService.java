package com.example.myapp.service;

import com.example.myapp.entity.exception.InputInvalidException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class InputValidationService {
    public void checkDataRange(Long startTime, Long endTime){
        if(startTime > endTime){

            throw new InputInvalidException(String.format("startTime is %s; endTime is %s. startTime should be smaller than endTime.", startTime, endTime)
);
        }
    }
    public void checkSymbol(@NotBlank String symbol, @NotNull KlineDataSourceService exchangeService) {
        List<String> symbolList  = exchangeService.getAllSymbols();
        if (!symbolList.contains(symbol)) {
            throw new InputInvalidException(String.format("Invalid symbol: %s", symbol));
        }

    }
}
