package com.example.calculator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculatorServiceTest {

    private CalculatorService service;

    @BeforeEach
    void setUp() {
        service = new CalculatorService();
    }

    @Test
    @DisplayName("add should handle positive, negative, and decimal values")
    void testAdd() {
        // Positive numbers
        assertEquals(new BigDecimal("5"), service.add(new BigDecimal("2"), new BigDecimal("3")));
        // Negative numbers
        assertEquals(new BigDecimal("-5"), service.add(new BigDecimal("-2"), new BigDecimal("-3")));
        // Mixed signs
        assertEquals(new BigDecimal("1"), service.add(new BigDecimal("-2"), new BigDecimal("3")));
        // Decimal numbers
        assertEquals(new BigDecimal("5.75"), service.add(new BigDecimal("2.5"), new BigDecimal("3.25")));
    }

    @Test
    @DisplayName("subtract should handle positive, negative, and decimal values")
    void testSubtract() {
        // Positive numbers
        assertEquals(new BigDecimal("2"), service.subtract(new BigDecimal("5"), new BigDecimal("3")));
        // Negative numbers
        assertEquals(new BigDecimal("1"), service.subtract(new BigDecimal("-2"), new BigDecimal("-3")));
        // Mixed signs
        assertEquals(new BigDecimal("-5"), service.subtract(new BigDecimal("-2"), new BigDecimal("3")));
        // Decimal numbers
        assertEquals(new BigDecimal("1.25"), service.subtract(new BigDecimal("3.5"), new BigDecimal("2.25")));
    }

    @Test
    @DisplayName("multiply should handle positive, negative, and decimal values")
    void testMultiply() {
        // Positive numbers
        assertEquals(new BigDecimal("15"), service.multiply(new BigDecimal("3"), new BigDecimal("5")));
        // Negative numbers
        assertEquals(new BigDecimal("6"), service.multiply(new BigDecimal("-2"), new BigDecimal("-3")));
        // Mixed signs
        assertEquals(new BigDecimal("-6"), service.multiply(new BigDecimal("-2"), new BigDecimal("3")));
        // Decimal numbers
        assertEquals(new BigDecimal("6.25"), service.multiply(new BigDecimal("2.5"), new BigDecimal("2.5")));
    }

    @Test
    @DisplayName("divide should handle normal cases and decimal results")
    void testDivide() {
        // Normal case
        assertEquals(new BigDecimal("3"), service.divide(new BigDecimal("6"), new BigDecimal("2")));
        // Decimal result (1 / 4 = 0.25)
        assertEquals(new BigDecimal("0.25"), service.divide(new BigDecimal("1"), new BigDecimal("4")));
        // Negative dividend
        assertEquals(new BigDecimal("-2.5"), service.divide(new BigDecimal("-5"), new BigDecimal("2")));
    }

    @Test
    @DisplayName("divide by zero should throw ArithmeticException")
    void testDivideByZero() {
        ArithmeticException ex = assertThrows(ArithmeticException.class, () ->
                service.divide(new BigDecimal("10"), BigDecimal.ZERO));
        assertEquals("Division by zero is not allowed", ex.getMessage());
    }
}
