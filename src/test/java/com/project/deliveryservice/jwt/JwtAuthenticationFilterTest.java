package com.project.deliveryservice.jwt;

import com.project.deliveryservice.common.constants.AuthConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationFilterTest {

    MockHttpServletRequest mockRequest;
    MockHttpServletResponse mockResponse;
    FilterChain mockFilterChain;
    AuthenticationManager mockAuthenticationManager;

    JwtAuthenticationFilter filter;

    @BeforeEach
    public void setup() {
        // 의존관계 모킹
        mockRequest = new MockHttpServletRequest();
        mockResponse = new MockHttpServletResponse();
        mockFilterChain = Mockito.mock(FilterChain.class);
        mockAuthenticationManager = Mockito.mock(AuthenticationManager.class);
        filter = new JwtAuthenticationFilter(mockAuthenticationManager);
    }

    @Test
    public void givenTokenNotInHeader_whenDoFilterInternal_thenAuthenticationManagerNotBeenCalled() throws ServletException, IOException {

        // setup
        when(mockAuthenticationManager.authenticate(any())).thenReturn(null);

        // action
        filter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        // verify
        verify(mockAuthenticationManager, never()).authenticate(any());
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
    }

    @Test
    public void givenInvalidTokenHeader_whenDoFilterInternal_thenAuthenticationManagerNotBeenCalled() throws ServletException, IOException {

        // setup
        mockRequest.addHeader(AuthConstants.AUTHORIZATION_HEADER, "invalid token");
        when(mockAuthenticationManager.authenticate(any())).thenReturn(null);

        // action
        filter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        // then
        verify(mockAuthenticationManager, never()).authenticate(any());
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
    }

    @Test
    public void givenReturnNullAfterAuthenticateWithValidToken_whenDoFilterInternal_thenAuthenticationFromSecurityContextHolderIsNull() throws ServletException, IOException {

        // setup
        mockRequest.addHeader(AuthConstants.AUTHORIZATION_HEADER, AuthConstants.BEARER_PREFIX + "valid_token");
        JwtAuthenticationToken token = new JwtAuthenticationToken("valid_token");

        when(mockAuthenticationManager.authenticate(token)).thenReturn(null);

        // action
        filter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        // verify
        assertThat(SecurityContextHolder.getContext().getAuthentication(), nullValue());
    }

    @Test
    public void givenThrowAuthenticationException_whenDoFilterInternal_thenSecurityContextHolderIsNullAndClearContextBeenCalled() throws ServletException, IOException {

        // setup
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        MockedStatic<SecurityContextHolder> holder = Mockito.mockStatic(SecurityContextHolder.class);

        holder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        mockRequest.addHeader(AuthConstants.AUTHORIZATION_HEADER, AuthConstants.BEARER_PREFIX +"valid_token");
        JwtAuthenticationToken token = new JwtAuthenticationToken("valid_token");

        when(mockAuthenticationManager.authenticate(token)).thenThrow(new JwtInvalidException("time expired"));

        // action
        filter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        // verify
        holder.verify(SecurityContextHolder::clearContext, times(1));
        assertThat(SecurityContextHolder.getContext().getAuthentication(), nullValue());

        // clear static Mockito
        Mockito.clearAllCaches();
    }

    @Test
    public void givenValidToken_whenDoFilterInternal_thenSecurityContextHasAuthentication() throws ServletException, IOException {

        // setup
        mockRequest.addHeader(AuthConstants.AUTHORIZATION_HEADER, AuthConstants.BEARER_PREFIX + "valid_token");
        JwtAuthenticationToken token = new JwtAuthenticationToken("valid_token");
        JwtAuthenticationToken authenticatedToken = new JwtAuthenticationToken(
                "admin",
                "",
                Collections.singletonList(() -> "ADMIN")
        );

        when(mockAuthenticationManager.authenticate(token)).thenReturn(authenticatedToken);

        // action
        filter.doFilterInternal(mockRequest, mockResponse, mockFilterChain);

        // verify
        assertThat(SecurityContextHolder.getContext().getAuthentication(), equalTo(authenticatedToken));
    }
}
