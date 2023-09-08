package com.bvb.bet.replica.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("stock.ponder")
@Data
public class PropertiesConfig {
    private double tlv;
    private double snp;
    private double h2o;
    private double sng;
    private double brd;
    private double snn;
    private double m;
    private double tgn;
    private double fp;
}
