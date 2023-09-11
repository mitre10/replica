package com.bvb.bet.replica.service;

import com.bvb.bet.replica.config.PropertiesConfig;
import com.bvb.bet.replica.model.Stock;
import com.bvb.bet.replica.model.StockDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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

    public String getFractions() {

        List<Stock> sortedStockList = stockList.stream().sorted(Comparator.comparing(Stock::getValue).reversed()).toList();
        List<StockDTO> stockDtoList = computePonders(sortedStockList);

        String htmlHeader = getHtlmContent("src/main/resources/static/ResponseHeader.txt");

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(htmlHeader);

        for (StockDTO stockDTO : stockDtoList) {
            stringBuilder.append("<p> For stock ");
            stringBuilder.append(stockDTO.getSymbol());
            stringBuilder.append(" you need to buy ");
            stringBuilder.append(stockDTO.getNumberOfSharesToBuy());
            stringBuilder.append(" stock at ");
            stringBuilder.append(stockDTO.getValue());
            stringBuilder.append(" lei per stock. Purchase amount ");
            stringBuilder.append(stockDTO.getPurchaseAmount());
            stringBuilder.append(" lei.");
            stringBuilder.append("<p> </br>");
        }

        String htmlFooter = String.format(getHtlmContent("src/main/resources/static/ResponseFooter.txt"),
                stockDtoList.stream().map(StockDTO::getPurchaseAmount).reduce(BigDecimal.ZERO, BigDecimal::add));

        stringBuilder.append(htmlFooter);

        return stringBuilder.toString();
    }

    private String getHtlmContent(String fileName) {
        StringBuilder contentBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new FileReader(fileName));
            String str;
            while ((str = in.readLine()) != null) {
                contentBuilder.append(str);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return contentBuilder.toString();
    }

    public List<StockDTO> computePonders(List<Stock> stocks) {
        List<StockDTO> stockDtoList = new ArrayList<>();

        calculatePonderCompensator();

        var maxPriceStock = stocks.get(0);

        StockDTO maxPriceStockDTO = new StockDTO();
        maxPriceStockDTO.setPonder(calculateFinalPonder(maxPriceStock.getPonder()));
        maxPriceStockDTO.setNumberOfSharesToBuy(1);
        maxPriceStockDTO.setPurchaseAmount(maxPriceStock.getValue().setScale(2, RoundingMode.HALF_EVEN));
        maxPriceStockDTO.setDescription(maxPriceStock.getDescription());
        maxPriceStockDTO.setSymbol(maxPriceStock.getSymbol());
        maxPriceStockDTO.setValue(maxPriceStock.getValue());

        stockDtoList.add(maxPriceStockDTO);

        for (Stock stock : stocks) {
            if (stock == maxPriceStock) {
                continue;
            }

            StockDTO stockDto = new StockDTO();
            stockDto.setDescription(stock.getDescription());
            stockDto.setSymbol(stock.getSymbol());
            stockDto.setValue(stock.getValue());

            stockDto.setPonder(calculateFinalPonder(stock.getPonder()));
            stockDto.setPurchaseAmount(calculatePurchaseAmount(stockDto, maxPriceStockDTO));
            stockDto.setNumberOfSharesToBuy(calculateNumberOfShares(stockDto));

            stockDtoList.add(stockDto);
        }

        return stockDtoList;
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

    private double calculateNumberOfShares(StockDTO stockDto) {
        var pricePerStock = stockDto.getValue();
        var totalAmount = stockDto.getPurchaseAmount();

        return totalAmount.divide(pricePerStock, 2, RoundingMode.HALF_EVEN).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }

    private BigDecimal calculatePurchaseAmount(StockDTO currentStockDto, StockDTO maxPriceStockDto) {
        return BigDecimal.valueOf(currentStockDto.getPonder()).multiply(maxPriceStockDto.getValue()).divide(BigDecimal.valueOf(maxPriceStockDto.getPonder()), 2, RoundingMode.HALF_EVEN).setScale(2, RoundingMode.HALF_EVEN);
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
}
