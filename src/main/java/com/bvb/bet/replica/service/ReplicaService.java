package com.bvb.bet.replica.service;

import com.bvb.bet.replica.config.PropertiesConfig;
import com.bvb.bet.replica.model.Stock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static com.bvb.bet.replica.controller.PriceController.stockList;

@Slf4j
@Service
public class ReplicaService {
    private double ponderCompensator;
    @Autowired
    private PropertiesConfig propertiesConfig;

    public List<Stock> getStockList() {
        Stock tlv = new Stock("TLV", "BANCA TRANSILVANIA S.A.", BigDecimal.valueOf((0)), propertiesConfig.getTlv());
        Stock snp = new Stock("SNP", "OMV PETROM S.A.", BigDecimal.valueOf(0), propertiesConfig.getSnp());
        Stock h20 = new Stock("H2O", "S.P.E.E.H. HIDROELECTRICA S.A.", BigDecimal.valueOf(0), propertiesConfig.getH2o());
        Stock sng = new Stock("SNG", "S.N.G.N. ROMGAZ S.A.", BigDecimal.valueOf(0), propertiesConfig.getSng());
        Stock brd = new Stock("BRD", "BRD - GROUPE SOCIETE GENERALE S.A.", BigDecimal.valueOf(0), propertiesConfig.getBrd());
        Stock snn = new Stock("SNN", "S.N. NUCLEARELECTRICA S.A.", BigDecimal.valueOf(0), propertiesConfig.getSnn());
        Stock m = new Stock("M", "MedLife S.A.", BigDecimal.valueOf(0), propertiesConfig.getM());
        Stock tgn = new Stock("TGN", "S.N.T.G.N. TRANSGAZ S.A.", BigDecimal.valueOf(0), propertiesConfig.getTgn());
        Stock fp = new Stock("FP", "FONDUL PROPRIETATEA", BigDecimal.valueOf(0), propertiesConfig.getFp());

        return new LinkedList<>(List.of(snp, tlv, h20, sng, brd, snn, m, tgn, fp));
    }

    public StringBuilder getFractions() {
        var stocks = stockList;

        var sortedStockList = stocks.stream().sorted(Comparator.comparing(Stock::getValue).reversed()).toList();
        var result = computePonders(sortedStockList);

        StringBuilder stringBuilder = new StringBuilder("You need to buy the following: \n");
        for (Stock stock : result) {

            stringBuilder.append("For stock ")
                    .append(stock.getSymbol())
                    .append(" you need to buy ")
                    .append(stock.getNumberOfSharesToBuy())
                    .append(" stocks ")
                    .append("for ")
                    .append(stock.getValue())
                    .append(" per stock")
                    .append(" [total amount: ")
                    .append(stock.getPurchaseAmount())
                    .append(" lei]")
                    .append("\n");
        }

        stringBuilder
                .append("TOTAL AMOUNT NEEDED: ")
                .append(result.stream().map(Stock::getPurchaseAmount).reduce(BigDecimal.ZERO, BigDecimal::add));

        return stringBuilder;
    }

    public List<Stock> computePonders(List<Stock> stocks) {

        calculatePonderCompensator();

        var maxPriceStock = stocks.get(0);
        log.info("Stock {}: initial ponder was {}, after compensation will be {} ", maxPriceStock.getSymbol(), maxPriceStock.getPonder(), calculateFinalPonder(maxPriceStock.getPonder()));
        maxPriceStock.setPonder(calculateFinalPonder(maxPriceStock.getPonder()));
        maxPriceStock.setNumberOfSharesToBuy(1);
        maxPriceStock.setPurchaseAmount(maxPriceStock.getValue().setScale(2, RoundingMode.HALF_EVEN));

        for (Stock stock : stocks) {
            if (stock == maxPriceStock) {
                continue;
            }

            log.info("Stock {}: initial ponder was {}, after compensation will be {} ", stock.getSymbol(), stock.getPonder(), calculateFinalPonder(calculateFinalPonder(stock.getPonder())));

            stock.setPonder(calculateFinalPonder(stock.getPonder()));
            stock.setPurchaseAmount(calculatePurchaseAmount(stock, maxPriceStock));
            calculateNumberOfShares(stock);
        }

        log.info("Total ponder is {}", stocks.stream().map(Stock::getPonder).reduce(Double::sum).get());

        return stocks;
    }

    private double calculateFinalPonder(double ponder) {
        return ponder + ponderCompensator;
    }

    private void calculatePonderCompensator() {
        var totalPonder = getStockList().stream().map(Stock::getPonder).reduce(0.0, Double::sum);
        var remainingPonder = 100.00 - totalPonder;

        ponderCompensator = BigDecimal.valueOf(remainingPonder).divide(BigDecimal.valueOf(getStockList().size()), 2, RoundingMode.HALF_EVEN).doubleValue();

        log.info("Ponder compensator is {}", ponderCompensator);
    }

    private void calculateNumberOfShares(Stock stock) {
        var pricePerStock = stock.getValue();
        var totalAmount = stock.getPurchaseAmount();

        var numberOfShares = totalAmount.divide(pricePerStock, 2, RoundingMode.HALF_EVEN).setScale(2, RoundingMode.HALF_EVEN);
        stock.setNumberOfSharesToBuy(numberOfShares.doubleValue());
    }

//    private void calculateNumberOfShares(Stock stock) {
//        var pricePerStock = stock.getValue();
//        var totalAmount = stock.getPurchaseAmount();
//
//        if (pricePerStock.compareTo(totalAmount) > 0) {
//            log.info("Setting number of shares to 1 because for stock {}, price per share [{}] si bigger than total amount [{}]", stock.getSymbol(), stock.getValue(), stock.getPurchaseAmount());
//            stock.setNumberOfSharesToBuy(1);
//
//            var calibrationAmount = pricePerStock.subtract(totalAmount);
//            log.info("Setting calibration amount to {}", calibrationAmount);
//            stock.setCalibrationAmount(calibrationAmount);
//
//            stock.setPurchaseAmount(totalAmount.add(calibrationAmount));
//        } else {
//            var numberOfShares = totalAmount.divide(pricePerStock, 2, RoundingMode.HALF_EVEN).setScale(2, RoundingMode.HALF_EVEN);
//            var integerPart = numberOfShares.intValue();
//            var fractionalPart = numberOfShares.subtract(BigDecimal.valueOf(integerPart)).doubleValue();
//
//            if (fractionalPart > 0.50) {
//                stock.setNumberOfSharesToBuy(integerPart + 1);
//                stock.setCalibrationAmount(totalAmount.subtract(pricePerStock));
//                stock.setPurchaseAmount(totalAmount.add(stock.getCalibrationAmount()));
//
//            } else {
//                stock.setNumberOfSharesToBuy(numberOfShares.intValue());
//                stock.setCalibrationAmount(totalAmount.subtract(pricePerStock));
//                stock.setPurchaseAmount(totalAmount.add(stock.getCalibrationAmount()));
//
//            }
//
//            log.info("Stock {} has {} per 1 unit. But the amount to buy is: {}", stock.getSymbol(), stock.getValue(), stock.getPurchaseAmount());
//        }
//    }

    private BigDecimal calculatePurchaseAmount(Stock currentStock, Stock maxPriceStock) {
        return BigDecimal.valueOf(currentStock.getPonder()).multiply(maxPriceStock.getValue()).divide(BigDecimal.valueOf(maxPriceStock.getPonder()), 2, RoundingMode.HALF_EVEN).setScale(2, RoundingMode.HALF_EVEN);
    }
}
