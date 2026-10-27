package com.example.calculator.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CalculatorControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/v1/add with valid params returns 200 and calculation result")
    void testAddEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/add")
                        .param("a", "2")
                        .param("b", "3")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("add"))
                .andExpect(jsonPath("$.a").value(2))
                .andExpect(jsonPath("$.b").value(3))
                .andExpect(jsonPath("$.result").value(5));
    }

    @Test
    @DisplayName("GET /api/v1/subtract with valid params returns 200")
    void testSubtractEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/subtract")
                        .param("a", "10")
                        .param("b", "4")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("subtract"))
                .andExpect(jsonPath("$.result").value(6));
    }

    @Test
    @DisplayName("GET /api/v1/multiply with valid params returns 200")
    void testMultiplyEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/multiply")
                        .param("a", "4")
                        .param("b", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operation").value("multiply"))
                .andExpect(jsonPath("$.result").value(20));
    }

    @Test
    @DisplayName("GET /api/v1/divide by zero returns 400 and DIVISION_BY_ZERO error")
    void testDivideByZeroEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/divide")
                        .param("a", "1")
                        .param("b", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("DIVISION_BY_ZERO"))
                .andExpect(jsonPath("$.message").value("Division by zero is not allowed"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /api/v1/add with missing parameter returns 400 and MISSING_PARAMETER error")
    void testMissingParameterEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/add")
                        .param("a", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MISSING_PARAMETER"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /api/v1/add with invalid parameter returns 400 and INVALID_PARAMETER error")
    void testInvalidParameterEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/add")
                        .param("a", "abc")
                        .param("b", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_PARAMETER"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /actuator/health returns 200 and UP status")
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
