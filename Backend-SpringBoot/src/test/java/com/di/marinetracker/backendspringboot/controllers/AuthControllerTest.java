//package com.di.marinetracker.backendspringboot.controllers;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(AuthController.class) // Specify the controller
//class AuthControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Test
//    void testLogin_returnsOk() throws Exception {
//        mockMvc.perform(post("/api/auth/login"))
//                .andExpect(status().isOk());
//    }
//}