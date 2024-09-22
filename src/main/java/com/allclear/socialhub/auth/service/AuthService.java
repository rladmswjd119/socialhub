package com.allclear.socialhub.auth.service;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.allclear.socialhub.user.dto.UserLoginRequest;

public interface AuthService {

    HttpHeaders createHeaders(String username);

}
