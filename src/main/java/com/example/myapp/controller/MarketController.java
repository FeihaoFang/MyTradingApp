package com.example.myapp.controller;
import com.example.myapp.entity.KlineData;
import com.example.myapp.service.*;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class MarketController {
    @Autowired
    private KlineDataLoadService klineDataService;

    @Autowired
    private InputValidationService inputValidationService;

    @Autowired
    private KlineDataRetrieveService klineDataRetrieveService;

    @Autowired
    private Map<String, KlineDataSourceService> exchangeServiceFactory;

    /**
     * Handles POST requests for loading Kline data into the database.
     * <p>
     * This method validates the input parameters and invokes the multi-threaded data loading service
     * based on the provided exchange name, symbol, and time range.
     * </p>
     *
     * @param exchangeName the name of the exchange
     * @param symbol       the trading symbol
     * @param startTime    the start time for data loading (in milliseconds)
     * @param endTime      the end time for data loading (in milliseconds)
     */
    @PostMapping("/klinedata") // 201
    public void loadKlineData(
            @RequestParam String exchangeName,
            @RequestParam String symbol,
            @RequestParam Long startTime,
            @RequestParam Long endTime) {
        KlineDataSourceService exchangeService = exchangeServiceFactory.get(exchangeName);
        inputValidationService.checkDataRange(startTime, endTime);
        inputValidationService.checkSymbol(symbol, exchangeService);
        klineDataService.multiThreadLoadData(symbol, startTime, endTime, exchangeService);
    }
    /**
     * Handles GET requests for retrieving Kline data.
     * <p>
     * This method validates the input parameters and is intended to retrieve Kline data based on the
     * provided exchange name, symbol, time range, and data interval.
     * </p>
     *
     * @param exchangeName the name of the exchange
     * @param symbol       the trading symbol
     * @param startTime    the start time for data retrieval (in milliseconds)
     * @param endTime      the end time for data retrieval (in milliseconds)
     * @param interval     the data interval
     * @return
     */
    @GetMapping("/klinedata")
    public @NotEmpty List<KlineData> retrieveKlineData(
            @RequestParam String exchangeName,
            @RequestParam String symbol,
            @RequestParam Long startTime,
            @RequestParam Long endTime,
            @RequestParam String interval
    ) {
        KlineDataSourceService exchangeService = exchangeServiceFactory.get(exchangeName);
        inputValidationService.checkDataRange(startTime, endTime);
        inputValidationService.checkSymbol(symbol, exchangeService);
        List<KlineData> dataList =  klineDataRetrieveService.retrieveData(startTime,  endTime,  symbol);
        return klineDataRetrieveService.aggregation(dataList, interval);
    }

}
