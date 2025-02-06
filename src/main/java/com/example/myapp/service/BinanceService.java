package com.example.myapp.service;
import java.util.*;
import com.example.myapp.entity.KlineData;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.math.BigDecimal;

@Validated
@Service("Binance")
public class BinanceService extends KlineDataSourceService {
    @Value("${binance.klineApi.url.template}")
    private String apiUrlTemplate;

    @Value("${binance.livePrice.url}")
    private String livePriceUrl;

    @Value("${binance.default.interval}")
    private String defaultInterval;

    @Value("${binance.default.limit}")
    private int defaultLimit;



    private static final Logger logger = LogManager.getLogger(Service.class);

    @Override
    public List<String> getAllSymbols(){
        ResponseEntity<List> response = restTemplate.getForEntity(livePriceUrl,List.class);
        List<Map<String, Object>> responseBody = (List<Map<String, Object>>) response.getBody();

        if (responseBody == null || responseBody.isEmpty()) {
            logger.error("Invalid response from Binance API: {}", responseBody);
            throw new RuntimeException("Failed to fetch symbols from Binance API");
        }
        List<String> symbolNames = new ArrayList<>();
        for (Map<String, Object> symbol : responseBody) {

            String symbolName = (String) symbol.get("symbol");
            if (symbolName != null) {
                symbolNames.add(symbolName);
            } else {
                logger.warn("Encountered a symbol with null or missing name: {}", symbol);
            }
        }

        return symbolNames;

    }


    @Override
    public String buildApiUrl(String symbol, long startTime, long endTime) {
        return String.format(apiUrlTemplate, symbol, defaultInterval, startTime, endTime, defaultLimit);
    }


    private KlineData parseRow(String[] row, String symbol) {
        if (row.length < 12) {
            logger.error("Invalid row: {} with invalid length: {}, expected 12", Arrays.toString(row), row.length);
            return null;
        }
        KlineData klineData = new KlineData();
        klineData.setOpenTime(Long.parseLong(row[0]));
        klineData.setOpenPrice(new BigDecimal(row[1]));
        klineData.setHighPrice(new BigDecimal(row[2]));
        klineData.setLowPrice(new BigDecimal(row[3]));
        klineData.setClosePrice(new BigDecimal(row[4]));
        klineData.setVolume(new BigDecimal(row[5]));
        klineData.setCloseTime(Long.parseLong(row[6]));
        klineData.setQuoteAssetVolume(new BigDecimal(row[7]));
        klineData.setNumberOfTrades(Integer.parseInt(row[8]));
        klineData.setTakerBuyBaseVolume(new BigDecimal(row[9]));
        klineData.setTakerBuyQuoteVolume(new BigDecimal(row[10]));
        klineData.setSymbol(symbol);
        return klineData;
    }

    @Override
    protected List<KlineData> parseResponseBody (@NotNull Object response, @NotBlank String symbol ) {
        // binance kline api always returns String[][] as response body
        String[][] responseBody = (String[][])response;
        logger.info(String.format("received %d kline data", responseBody.length));

        List<KlineData> klineDataList = Arrays.stream(responseBody)
            .parallel()
            .map(body -> parseRow(body, symbol))
            .filter(Objects::nonNull) // invalid input will return null
            .toList();

        logger.info("processed {} kline data", klineDataList.size());
        if (responseBody.length != klineDataList.size()){
            logger.error("Failed to process {} of kline records", (responseBody.length- klineDataList.size()));
        }

        return klineDataList;
    }


}
