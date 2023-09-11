package com.bvb.bet.replica.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Stock {

    private String symbol;
    private String description;
    private BigDecimal value;
    private double ponder;

    public Stock() {
    }

    public Stock(String symbol, String description, BigDecimal value, double ponder) {
        this.symbol = symbol;
        this.description = description;
        this.value = value;
        this.ponder = ponder;
    }
}
