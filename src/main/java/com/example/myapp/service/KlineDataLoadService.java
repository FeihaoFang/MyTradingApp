package com.example.myapp.service;

import com.example.myapp.entity.KlineData;
import com.example.myapp.enums.Interval;
import com.example.myapp.mapper.KlineDataMapper;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.LongStream;

@Validated
@Service
public class KlineDataLoadService {

    @Autowired
    private KlineDataMapper klineDataMapper;

    @Value("${binance.default.interval}")
    private String defaultInterval;

    @Value("${binance.default.limit}")
    private int defaultLimit;

    /**
     * This function is to load data based on input klineDataSourceService, and load into database
     * Note: most  exchanges has limitaion on one time query. for exmaple, biance only gives 500 at most.
     * The function divides large amonut of data into pieces and parallel loading them.
     * @param symbol
     * @param startTime
     * @param endTime
     * @param exchangeService
     */
    public void multiThreadLoadData(@NotBlank String symbol, @NotNull @Min(0) Long startTime, @NotNull @Min(0) Long endTime, @NotNull KlineDataSourceService exchangeService) {
        long intervalMs = Interval.fromLabel(defaultInterval).getMilliseconds();
        long timeSpanPerCall = intervalMs * defaultLimit;

        LongStream.range(0, (long) Math.ceil((double) (endTime - startTime) / timeSpanPerCall))
                .parallel()
                .mapToObj(i -> {
                    long batchStartTime = startTime + i * timeSpanPerCall;
                    long batchEndTime = Math.min(batchStartTime + timeSpanPerCall, endTime);
                    return new long[]{batchStartTime, batchEndTime}; // 定义 batch
                })
                .forEach(batch -> {
                    long batchStartTime = batch[0];
                    long batchEndTime = batch[1];
                    List<KlineData> batchData = exchangeService.getData(symbol, batchStartTime, batchEndTime);
                    klineDataMapper.batchInsert(batchData);
                });
    }
}
