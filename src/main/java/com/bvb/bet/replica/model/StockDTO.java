package com.bvb.bet.replica.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockDTO {

    private String symbol;
    private String description;
    private BigDecimal value;
    private double ponder;
    private double numberOfSharesToBuy;
    private BigDecimal purchaseAmount;
    private BigDecimal calibrationAmount;
}
