package com.bvb.bet.replica.controller;

import com.bvb.bet.replica.model.Stock;
import com.bvb.bet.replica.service.PriceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/prices")
@EnableScheduling
@Slf4j
public class PriceController {
    public static List<Stock> stockList;

    @Autowired
    private PriceService priceService;

    @GetMapping()
    @Scheduled(initialDelay = 1000, fixedRate = 60000)
    public ResponseEntity<String> getPrices() {
        log.info("Refreshing stock values at {}", new Date());

        stockList = priceService.getPriceValues();

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
