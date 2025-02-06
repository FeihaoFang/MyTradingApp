package com.example.myapp.service;

import com.example.myapp.entity.KlineData;
import com.example.myapp.enums.Interval;
import com.example.myapp.mapper.KlineDataMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.data.redis.core.RedisTemplate;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Validated
@Service
public class KlineDataRetrieveService {

    private static final Logger logger = LogManager.getLogger(Service.class);

    @Autowired
    private KlineDataMapper klineDataMapper;

    @Value("${binance.default.interval}")
    private String defaultInterval;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    /**
     * Retrieves Kline data for a given symbol and time range by first checking the Redis cache and,
     * if necessary, falling back to the database.
     * <p>
     * The method performs the following steps:
     * <ul>
     *   <li>Calculates the aligned start and end times based on the default interval.</li>
     *   <li>Retrieves cached Kline data from a Redis sorted set using a score range query.</li>
     *   <li>Determines the expected number of data points based on the time range and interval.</li>
     *   <li>If the cached data is insufficient, queries the database for the missing Kline data.</li>
     *   <li>Caches any newly retrieved data into Redis and sets an expiration time.</li>
     *   <li>Merges the data from both the cache and the database, ensuring that data points are unique
     *       and ordered by their open time.</li>
     * </ul>
     * </p>
     *
     * @param startTime the starting timestamp (in milliseconds) for retrieving Kline data; must not be {@code null}
     * @param endTime   the ending timestamp (in milliseconds) for retrieving Kline data; must not be {@code null}
     * @param symbol    the trading symbol for which Kline data is being retrieved; must not be blank
     * @return a non-empty list of {@link KlineData} objects containing the merged Kline data for the given range
     */
    public @NotEmpty List<KlineData> retrieveData(@NotNull Long startTime, @NotNull  Long endTime, @NotBlank String symbol) {
        String redisKey = "klineData:" + symbol + ":" + defaultInterval;
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        long alignedStartTime = getBucketStartTime(startTime, defaultInterval);
        long alignedEndTime = getBucketStartTime(endTime, defaultInterval);
        if (alignedEndTime < endTime) {
            alignedEndTime += Interval.fromLabel(defaultInterval).getMilliseconds();
        }
        Set<Object> resultSet = zSetOps.rangeByScore(redisKey, alignedStartTime, alignedEndTime);
        long intervalMillis = Interval.fromLabel(defaultInterval).getMilliseconds();
        int expectedCount = (int) ((alignedEndTime - alignedStartTime) / intervalMillis)  ;

        List<KlineData> cachedData = resultSet.stream()
                .map(obj -> (KlineData) obj)
                .collect(Collectors.toList());

        logger.info("Expected data count: {}", expectedCount);
        logger.info("Cached data count: {}", cachedData.size());


        if (cachedData.size() < expectedCount) {

            List<KlineData> dbData = klineDataMapper.findByPrimaryKey(alignedStartTime, alignedEndTime, symbol);
            logger.info("db data count: {}", dbData.size());

            for (KlineData data : dbData) {
                zSetOps.add(redisKey, data, data.getOpenTime());
            }
            redisTemplate.expire(redisKey, 1, TimeUnit.HOURS);


            Map<Long, KlineData> mergedMap = new TreeMap<>();
            for (KlineData data : cachedData) {
                mergedMap.put(data.getOpenTime(), data);
            }
            for (KlineData data : dbData) {
                mergedMap.put(data.getOpenTime(), data);
            }
            return new ArrayList<>(mergedMap.values());
        }

        return cachedData;
    }

    private long getBucketStartTime(@NotNull long timestamp, @NotBlank String interval) {
        long intervalMillis = Interval.fromLabel(interval).getMilliseconds();
        return (timestamp / intervalMillis) * intervalMillis ;
    }

    /**
     * Aggregates a list of KlineData objects into larger time buckets based on the specified interval.
     * <p>
     * This method groups the provided KlineData entries by aligning their open times to a bucket determined
     * by the given interval. For each bucket, it aggregates the data by:
     * <ul>
     *   <li>Using the bucket's start time as the open time for the aggregated data.</li>
     *   <li>Taking the close time from the last KlineData in the bucket.</li>
     *   <li>Using the open price from the first record and the close price from the last record.</li>
     *   <li>Determining the maximum high price and minimum low price within the bucket.</li>
     *   <li>Summing up volumes, quote asset volumes, taker buy base volumes, and taker buy quote volumes.</li>
     *   <li>Calculating the total number of trades by summing individual trade counts.</li>
     * </ul>
     * </p>
     *
     * @param dataList the list of KlineData objects to aggregate; must not be null
     * @param interval the aggregation interval (e.g., "1m", "5m"); must not be blank
     * @return a non-empty list of aggregated KlineData objects grouped by the specified interval
     */
    public @NotEmpty List<KlineData> aggregation(@NotNull List<KlineData> dataList, @NotBlank String interval) {
        List<KlineData> aggregatedList = new ArrayList<>();

        Map<Long, List<KlineData>> groupedData = dataList.stream().parallel()
                .collect(Collectors.groupingBy(data -> getBucketStartTime(data.getOpenTime(), interval)));
        for (Map.Entry<Long, List<KlineData>> entry : groupedData.entrySet()) {
            long bucketStartTime = entry.getKey();
            List<KlineData> bucket = entry.getValue();

            KlineData aggregated = new KlineData();
            aggregated.setOpenTime(bucketStartTime);
            aggregated.setCloseTime(bucket.get(bucket.size() - 1).getCloseTime());
            aggregated.setSymbol(bucket.get(0).getSymbol());

            aggregated.setOpenPrice(bucket.get(0).getOpenPrice());
            aggregated.setClosePrice(bucket.get(bucket.size() - 1).getClosePrice());
            aggregated.setHighPrice(bucket.stream().parallel().map(KlineData::getHighPrice).max(BigDecimal::compareTo).orElse(null));
            aggregated.setLowPrice(bucket.stream().parallel().map(KlineData::getLowPrice).min(BigDecimal::compareTo).orElse(null));
            aggregated.setVolume(bucket.stream().parallel().map(KlineData::getVolume).reduce(BigDecimal.ZERO, BigDecimal::add));
            aggregated.setQuoteAssetVolume(bucket.stream().parallel().map(KlineData::getQuoteAssetVolume).reduce(BigDecimal.ZERO, BigDecimal::add));
            aggregated.setNumberOfTrades(bucket.stream().parallel().mapToInt(KlineData::getNumberOfTrades).sum());
            aggregated.setTakerBuyBaseVolume(bucket.stream().parallel().map(KlineData::getTakerBuyBaseVolume).reduce(BigDecimal.ZERO, BigDecimal::add));
            aggregated.setTakerBuyQuoteVolume(bucket.stream().parallel().map(KlineData::getTakerBuyQuoteVolume).reduce(BigDecimal.ZERO, BigDecimal::add));

            aggregatedList.add(aggregated);
        }
        return aggregatedList;
    }
}
