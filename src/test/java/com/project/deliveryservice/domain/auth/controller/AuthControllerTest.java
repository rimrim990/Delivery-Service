package com.project.deliveryservice.domain.auth.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("login 은 토큰 없이 요청을 보내도 OK 상태를 반환한다.")
    public void givenWithoutToken_whenCallLogin_thenIsoOk() throws Exception {

        mockMvc.perform(
                post("/api/auth/login")
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("토큰이 존재하지 않으면 Forbidden 상태를 반환한다.")
    public void givenWithoutToken_whenCallNotExistsPath_thenIsForbidden() throws Exception {

        mockMvc.perform(
                post("/something-other")
                )
                .andExpect(status().isForbidden());
    }
}