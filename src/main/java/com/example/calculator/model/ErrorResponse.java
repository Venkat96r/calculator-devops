package com.example.calculator.model;

/**
 * Standard error response structure for client errors.
 *
 * @param error Error code identifying the failure category
 * @param message Human-readable message detailing the error
 * @param status HTTP status code (e.g. 400)
 */
public record ErrorResponse(String error, String message, int status) {}
