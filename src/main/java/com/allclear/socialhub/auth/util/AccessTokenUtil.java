package com.allclear.socialhub.auth.util;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.allclear.socialhub.auth.service.UserDetailsServiceImpl;
import com.allclear.socialhub.common.exception.CustomException;
import com.allclear.socialhub.common.exception.ErrorCode;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessTokenUtil {

	@Value("${JWT_SECRET_KEY}")
	private String SECRET_KEY;

	private final long EXPIRATION_TIME = 60*1000*30;

	private final String BEARER = "Bearer ";
	private final String AUTHORIZATION = "Authorization";

	private final UserDetailsServiceImpl userDetailsService;

	/**
	 * 1. 문자열을 SecretKey 타입으로 변환
	 * 작성자 : 김은정
	 *
	 * @return SecretKey key
	 */
	private SecretKey getSigningKey() {

		byte[] keyBytes = Decoders.BASE64.decode(this.SECRET_KEY);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	/**
	 * 2. JWT 토큰 생성
	 * 작성자 : 김은정
	 *
	 * @param username
	 * @return String accessToken
	 */
	public String createToken(String username) {

		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

		return BEARER + Jwts.builder()
				.claim("username", username)
				.issuedAt(now)
				.expiration(expiryDate)
				.signWith(this.getSigningKey())
				.compact();
	}

	public String extractToken(HttpServletRequest request) {
		String bearerToken = request.getHeader(AUTHORIZATION);
		if(bearerToken != null && bearerToken.startsWith(BEARER)) {
			return removeBearer(bearerToken);
		}

		return null;
	}

	/**
	 * 3. JWT 토큰에서 Payload 추출
	 * 작성자 : 김은정
	 *
	 * @param token
	 * @return Claims payload
	 */
	public Claims getClaims(String token) {
		try {
			Claims claims = Jwts.parser()
					.verifyWith(this.getSigningKey())
					.build()
					.parseSignedClaims(token)
					.getPayload();

			if (claims == null || claims.isEmpty()) {
				throw new CustomException(ErrorCode.JWT_CLAIMS_EMPTY);
			}

			return claims;

		} catch (ExpiredJwtException e) {
			throw new CustomException(ErrorCode.EXPIRED_JWT_TOKEN);
		} catch (Exception e) {
			throw new CustomException(ErrorCode.INVALID_JWT_TOKEN);
		}
	}

	public String removeBearer(String token) {
		if (!token.startsWith(BEARER)) {
			throw new CustomException(ErrorCode.INVALID_JWT_TOKEN);
		}

		return token.substring(BEARER.length());
	}

	/**
	 * JWT 토큰에서 비공개 클레임 username 추출
	 * 작성자 : 김은정
	 */
	public String extractUsername(Claims claims) {
		if (claims == null) {
			throw new CustomException(ErrorCode.INVALID_JWT_TOKEN);
		}
		return String.valueOf(claims.get("username"));
	}

	public Authentication getAuthentication(String noBearerToken) {
		String username = decodeUsername(noBearerToken);

		UserDetails userDetails = userDetailsService.loadUserByUsername(username);
		log.info("username = {}, password = {}", userDetails.getUsername(), userDetails.getPassword());

		return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());

	}

	private String decodeUsername(String noBearerToken) {
		return String.valueOf(getClaims(noBearerToken).get("username"));
	}

}
