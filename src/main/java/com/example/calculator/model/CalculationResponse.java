package com.example.calculator.model;

import java.math.BigDecimal;

/**
 * Immutable response model representing a calculation result.
 *
 * @param operation Name of the operation performed (e.g. add, subtract, multiply, divide)
 * @param a First operand
 * @param b Second operand
 * @param result Computed result of the operation
 */
public record CalculationResponse(String operation, BigDecimal a, BigDecimal b, BigDecimal result) {}
