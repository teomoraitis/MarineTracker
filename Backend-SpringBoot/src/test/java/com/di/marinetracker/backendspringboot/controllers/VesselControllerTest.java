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
//@WebMvcTest(VesselController.class)
//class VesselControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Test
//    void testGetAllVessels_returnsOk() throws Exception {
//        mockMvc.perform(get("/api/vessels"))
//                .andExpect(status().isOk());
//    }
//}