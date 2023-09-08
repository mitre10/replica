package com.bvb.bet.replica.service;

import com.bvb.bet.replica.model.Stock;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class PriceService {

    @Value("${price.url}")
    private String priceURL;
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ReplicaService replicaService;

    public List<Stock> getPriceValues() {
        List<Stock> stockList = replicaService.getStockList();

        for (Stock stock : stockList) {
            String response = restTemplate.getForObject(String.format(priceURL, stock.getSymbol()), String.class);
            Map stockPrice = new Gson().fromJson(response.replace(",", "."), Map.class);

            stock.setValue(BigDecimal.valueOf((Double) stockPrice.get("RON")));
        }
        return stockList;
    }
}
