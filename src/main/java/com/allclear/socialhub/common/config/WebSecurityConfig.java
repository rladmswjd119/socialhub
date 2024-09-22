package com.allclear.socialhub.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.allclear.socialhub.auth.filter.JwtFilter;
import com.allclear.socialhub.auth.util.AccessTokenUtil;

import lombok.RequiredArgsConstructor;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig {

	private final AccessTokenUtil accessTokenUtil;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(HttpMethod.POST,"/api/users",
														"/api/users/login",
														"/api/users/duplicate-check").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/token/**").permitAll()
						.anyRequest().authenticated())
				.addFilterBefore(new JwtFilter(accessTokenUtil), UsernamePasswordAuthenticationFilter.class);

		http
				.sessionManagement((session) -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS));


		return http.getOrBuild();
	}
}
