package com.example.myapp.entity;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import  java.math.BigDecimal;
@Validated
@Data
public class KlineData {

    @NotNull @Min(0)
    private Long openTime;
    @NotNull @Min(0)
    private Long closeTime;
    @NotEmpty
    private String symbol;
    @NotNull
    @Digits(integer = 20, fraction = 8)
    private BigDecimal openPrice;

    @NotNull
    @Digits(integer = 20, fraction = 8)
    private BigDecimal highPrice;

    @NotNull
    @Digits(integer = 20, fraction = 8)
    private BigDecimal lowPrice;

    @NotNull
    @Digits(integer = 20, fraction = 8)
    private BigDecimal closePrice;

    @NotNull
    @Digits(integer = 20, fraction = 8)
    private BigDecimal volume;

    @NotNull
    @Digits(integer = 20, fraction = 8)
    private BigDecimal quoteAssetVolume;

    @NotNull
    @Min(0)
    private Integer numberOfTrades;

    @NotNull
    @Digits(integer = 20, fraction = 8)
    private BigDecimal takerBuyBaseVolume;

    @NotNull
    @Digits(integer = 20, fraction = 8)
    private BigDecimal takerBuyQuoteVolume;
}
