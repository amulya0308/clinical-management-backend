package com.clinic.controller;

import com.clinic.dto.request.LoginRequest;
import com.clinic.dto.request.RegisterRequest;
import com.clinic.dto.response.AuthResponse;
import com.clinic.entity.Role;
import com.clinic.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void testRegister_Success() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("newpatient@clinic.com")
                .password("Password@123")
                .fullName("Alice Wonderland")
                .age(25)
                .gender("Female")
                .phone("+1-555-8888")
                .address("100 Wonderland Way")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .token("mock-jwt-token")
                .id(1L)
                .email(request.getEmail())
                .role(Role.PATIENT)
                .fullName(request.getFullName())
                .profileId(1L)
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.data.email").value("newpatient@clinic.com"));
    }

    @Test
    void testRegister_ValidationErrorOnInvalidEmail() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("invalid-email")
                .password("Password@123")
                .fullName("Alice")
                .age(25)
                .gender("Female")
                .phone("123456")
                .address("Road")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void testLogin_Success() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("admin@clinic.com")
                .password("Admin@123")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .token("mock-jwt-token-admin")
                .id(1L)
                .email(request.getEmail())
                .role(Role.ADMIN)
                .fullName("System Administrator")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.token").value("mock-jwt-token-admin"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }
}
