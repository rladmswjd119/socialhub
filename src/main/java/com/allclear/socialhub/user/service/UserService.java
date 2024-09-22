package com.allclear.socialhub.user.service;

import org.springframework.http.HttpHeaders;

import com.allclear.socialhub.auth.dto.UserDetailsImpl;
import com.allclear.socialhub.user.dto.UserInfoUpdateRequest;
import com.allclear.socialhub.user.dto.UserInfoUpdateResponse;
import com.allclear.socialhub.user.dto.UserJoinRequest;
import com.allclear.socialhub.user.dto.UserLoginRequest;

public interface UserService {

    void joinUser(UserJoinRequest request);

    boolean verifyUser(String storedCode, String authCode, String email);
    

    UserInfoUpdateResponse updateUserInfo(UserInfoUpdateRequest request, UserDetailsImpl userDetails);

    HttpHeaders userLogin(UserLoginRequest request);

    String userDuplicateCheck(String username);

}
