//package com.di.marinetracker.backendspringboot.controllers;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(ZoneOfInterestController.class)
//class ZoneOfInterestControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Test
//    void testGetZones_returnsOk() throws Exception {
//        mockMvc.perform(get("/api/zones"))
//                .andExpect(status().isOk());
//    }
//}