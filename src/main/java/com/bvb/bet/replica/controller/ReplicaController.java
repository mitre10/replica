package com.bvb.bet.replica.controller;

import com.bvb.bet.replica.service.ReplicaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController()
@RequestMapping("/replica")
public class ReplicaController {
    @Autowired
    private ReplicaService replicaService;

    @GetMapping("/fractions")
    public ResponseEntity<String> getReplica() {

        return new ResponseEntity<>(replicaService.getFractions(), HttpStatus.OK);
    }
}
