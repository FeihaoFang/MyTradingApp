package com.example.myapp;

import com.example.myapp.entity.KlineData;
import com.example.myapp.service.BinanceService;
import com.example.myapp.service.KlineDataRetrieveService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;

@SpringBootTest
class MyappApplicationTests {
	@Autowired
	private BinanceService binanceService;

	@Autowired
	private KlineDataRetrieveService klineDataRetrieveService;

	@Test
	void apiCallTest() {
		long startTime = Instant.now().toEpochMilli() - 1000000;
		long endTime = Instant.now().toEpochMilli();


		System.out.println(startTime);
		System.out.println(endTime);
	}

	@Test
	void retrieveTest(){
		long startTime = 1725509008480L;
		long endTime = 1737061008480L;
		String symbol = "BTCUSDT";
		List<KlineData> unaggregated = klineDataRetrieveService.retrieveData(startTime,endTime,symbol);
		List<KlineData> aggregated = klineDataRetrieveService.aggregation(unaggregated, "5m");


	}

}
