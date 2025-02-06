package com.example.myapp.mapper;

import com.example.myapp.entity.KlineData;
import org.apache.ibatis.annotations.*;
import java.util.List;
/**
 * Mapper interface for operations on the {@code kline_data} table.
 */
@Mapper
public interface KlineDataMapper {
    /**
     * Inserts a list of KlineData records into the database using batch operation.
     * <p>
     * This method uses MyBatis's foreach to perform a batch insert. If a record already exists,
     * the operation is ignored.
     * </p>
     *
     * @param dataList the list of {@link KlineData} records to be inserted; must not be null
     */
    @Insert({
            "<script>",
            "INSERT IGNORE INTO kline_data (open_time, close_time, symbol, open_price, high_price, low_price, close_price, volume, quote_asset_volume, number_of_trades, taker_buy_base_volume, taker_buy_quote_volume) VALUES ",
            "<foreach collection='list' item='data' separator=','>",
            "(#{data.openTime}, #{data.closeTime}, #{data.symbol}, #{data.openPrice}, #{data.highPrice}, #{data.lowPrice}, #{data.closePrice}, #{data.volume}, #{data.quoteAssetVolume}, #{data.numberOfTrades}, #{data.takerBuyBaseVolume}, #{data.takerBuyQuoteVolume})",
            "</foreach>",
            "</script>"
    })
    void batchInsert(@Param("list") List<KlineData> dataList);
    /**
     * Inserts a single {@link KlineData} record into the database.
     *
     * @param data the {@link KlineData} record to be inserted; must not be null
     */
    @Insert("INSERT INTO kline_data (open_time, close_time, symbol ,open_price, high_price, low_price, close_price, volume, quote_asset_volume, number_of_trades, taker_buy_base_volume, taker_buy_quote_volume) " +
            "VALUES (#{openTime}, #{closeTime},#{symbol}, #{openPrice}, #{highPrice}, #{lowPrice}, #{closePrice}, #{volume}, #{quoteAssetVolume}, #{numberOfTrades}, #{takerBuyBaseVolume}, #{takerBuyQuoteVolume} )")

    void insertKlineData(KlineData data);

    /**
     * Retrieves a list of {@link KlineData} records based on the provided time range and symbol.
     * <p>
     * The method queries records where the open time is greater than or equal to {@code openTime}
     * and the close time is less than or equal to {@code closeTime} for the specified symbol.
     * </p>
     *
     * @param openTime the lower bound of the open time (inclusive) in milliseconds; must not be null
     * @param closeTime the upper bound of the close time (inclusive) in milliseconds; must not be null
     * @param symbol the trading symbol for which the data is to be retrieved; must not be null
     * @return a list of {@link KlineData} records matching the criteria
     */
    @Select("SELECT * FROM kline_data WHERE open_time >= #{openTime} AND close_time <= #{closeTime} AND symbol = #{symbol}")
    @Results({
            @Result(property = "openTime", column = "open_time"),
            @Result(property = "closeTime", column = "close_time"),
            @Result(property = "symbol", column = "symbol"),
            @Result(property = "openPrice", column = "open_price"),
            @Result(property = "highPrice", column = "high_price"),
            @Result(property = "lowPrice", column = "low_price"),
            @Result(property = "closePrice", column = "close_price"),
            @Result(property = "volume", column = "volume"),
            @Result(property = "quoteAssetVolume", column = "quote_asset_volume"),
            @Result(property = "numberOfTrades", column = "number_of_trades"),
            @Result(property = "takerBuyBaseVolume", column = "taker_buy_base_volume"),
            @Result(property = "takerBuyQuoteVolume", column = "taker_buy_quote_volume"),
    })
    List<KlineData> findByPrimaryKey(@Param("openTime") Long openTime, @Param("closeTime") Long closeTime, @Param("symbol") String symbol);
    /**
     * Retrieves all {@link KlineData} records from the database.
     *
     * @return a list of all {@link KlineData} records
     */
    @Select("SELECT * FROM kline_data")
    List<KlineData> findAll();
}
