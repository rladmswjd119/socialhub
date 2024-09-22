package com.allclear.socialhub.auth.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.allclear.socialhub.auth.service.AuthService;
import com.allclear.socialhub.auth.util.RefreshTokenUtil;
import com.allclear.socialhub.common.exception.CustomException;
import com.allclear.socialhub.common.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/token")
public class TokenController {

    private final RefreshTokenUtil refreshTokenUtil;
    private final AuthService authService;

    @GetMapping("/refresh")
    public ResponseEntity<String> getNewAccessToekn(@RequestHeader("refreshTokenName") String username) {
        boolean isRefreshToken = refreshTokenUtil.verifyRefreshToken(username);
        if(isRefreshToken) {
            log.error("재로그인이 필요합니다.");
            throw new CustomException(ErrorCode.EXPIRED_JWT_TOKEN);
        }

        HttpHeaders httpHeaders = authService.createHeaders(username);
        return ResponseEntity.ok().headers(httpHeaders).body("새로운 Access Token이 발급되었습니다.");
    }

}
