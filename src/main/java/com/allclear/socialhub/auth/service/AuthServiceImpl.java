package com.allclear.socialhub.auth.service;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.allclear.socialhub.auth.util.AccessTokenUtil;
import com.allclear.socialhub.auth.util.RefreshTokenUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AccessTokenUtil accessTokenUtil;
    private final RefreshTokenUtil refreshTokenUtil;

    @Override
    public HttpHeaders createHeaders(String username) {
        String accessToken = accessTokenUtil.createToken(username);
        refreshTokenUtil.createRefreshToken(username);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", accessToken);
        httpHeaders.add("refreshTokenName", username);

        return httpHeaders;
    }


}
