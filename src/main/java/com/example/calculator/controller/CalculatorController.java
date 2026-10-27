package com.example.calculator.controller;

import com.example.calculator.model.CalculationResponse;
import com.example.calculator.service.CalculatorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * REST controller exposing basic calculator endpoints under /api/v1.
 */
@RestController
@RequestMapping("/api/v1")
public class CalculatorController {

    private final CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @GetMapping("/add")
    public CalculationResponse add(@RequestParam BigDecimal a, @RequestParam BigDecimal b) {
        BigDecimal result = calculatorService.add(a, b);
        return new CalculationResponse("add", a, b, result);
    }

    @GetMapping("/subtract")
    public CalculationResponse subtract(@RequestParam BigDecimal a, @RequestParam BigDecimal b) {
        BigDecimal result = calculatorService.subtract(a, b);
        return new CalculationResponse("subtract", a, b, result);
    }

    @GetMapping("/multiply")
    public CalculationResponse multiply(@RequestParam BigDecimal a, @RequestParam BigDecimal b) {
        BigDecimal result = calculatorService.multiply(a, b);
        return new CalculationResponse("multiply", a, b, result);
    }

    @GetMapping("/divide")
    public CalculationResponse divide(@RequestParam BigDecimal a, @RequestParam BigDecimal b) {
        BigDecimal result = calculatorService.divide(a, b);
        return new CalculationResponse("divide", a, b, result);
    }
}
