package com.allclear.socialhub.auth.util;

import java.security.SecureRandom;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import com.allclear.socialhub.common.exception.CustomException;
import com.allclear.socialhub.common.exception.ErrorCode;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RefreshTokenUtil {

    @Value("${JWT_SECRET_KEY}")
    private String SECRET_KEY;
    private final String BEARER = "Bearer ";
    private final String AUTHORIZATION = "Authorization";
    private final int KEY_LEN = 100000000;

    // 7일
    private final long EXPIRATION_TIME = 60*1000*60*24*7;

    private final AccessTokenUtil accessTokenUtil;

    private final RedisTemplate<String, String> redisTemplate;

    private SecretKey getSigningKey() {

        byte[] keyBytes = Decoders.BASE64.decode(this.SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    public void createRefreshToken(String username) {
        boolean verifyRefreshToekn = verifyRefreshToken(username);
        if(verifyRefreshToekn) {
            throw new CustomException(ErrorCode.EXIST_REFRESH_TOKEN);
        }

        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + EXPIRATION_TIME);
        String secureRandom = createSecureRandom(KEY_LEN);
        String refreshToken = Jwts.builder()
                .claim("refreshToken", secureRandom)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSigningKey())
                .compact();

        log.info("refresh token : {}", refreshToken);
        saveRefreshToken(username, refreshToken);
    }

    public boolean verifyRefreshToken(String username) {
        String refreshToken = redisTemplate.opsForValue().get(username);
        if(refreshToken == null) {
            log.info("Refresh token이 존재하지 않습니다.");
            return false;
        }

        log.info("Refresh token이 존재합니다.");
        return true;
    }

    private String createSecureRandom(int limit) {
        SecureRandom secureRandom = new SecureRandom();
        return String.valueOf(secureRandom.nextInt(limit));

    }

    private void saveRefreshToken(String username, String refreshToken) {
        redisTemplate.opsForValue().set("username", refreshToken);
    }

}
