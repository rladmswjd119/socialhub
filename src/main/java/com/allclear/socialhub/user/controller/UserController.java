package com.allclear.socialhub.user.controller;

import com.allclear.socialhub.common.exception.CustomException;
import com.allclear.socialhub.user.dto.UserJoinRequest;
import com.allclear.socialhub.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 사용자 회원가입을 처리합니다.
     * 작성자: 배서진
     *
     * @param request 사용자 회원가입 요청 데이터(유효성 검사됨)
     * @return 성공 메시지 또는 오류 메시지
     */
    @PostMapping("")
    public ResponseEntity<String> joinUser(@Valid @RequestBody UserJoinRequest request) {

        try {
            userService.joinUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 완료되었습니다.");
        } catch (CustomException e) {
            throw e;
        }
    }


}
