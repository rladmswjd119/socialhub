package com.allclear.socialhub.auth.filter;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.allclear.socialhub.auth.util.AccessTokenUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final AccessTokenUtil accessTokenUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        log.info("api = {}", request.getRequestURI());

        String noBearerToken = accessTokenUtil.extractToken(request);

        log.info("No bearer token = {}", noBearerToken);

        if(noBearerToken != null) {
            Authentication authentication = accessTokenUtil.getAuthentication(noBearerToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("authentication = {}", authentication);
        }

        filterChain.doFilter(request, response);

    }

}
