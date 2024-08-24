package com.allclear.socialhub.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // USER
    USER_NOT_EXIST(HttpStatus.NOT_FOUND, "가입되지 않은 아이디입니다."),
    EMAIL_DUPLICATION(HttpStatus.CONFLICT, "중복된 이메일입니다."),
    USERNAME_DUPLICATION(HttpStatus.BAD_REQUEST, "중복된 계정명입니다."),
    PASSWORD_NOT_VALID(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    INVALID_PASSWORD_LENGTH(HttpStatus.BAD_REQUEST, "비밀번호는 10자 이상 설정해야 합니다."),
    INVALID_PASSWORD_PATTERN(HttpStatus.BAD_REQUEST, "비밀번호는 10자 이상, 20자 이하 영문자, 숫자, 특수문자(!@#$%^&*)중 2가지 이상 포함해야 합니다."),

    // POST
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시물입니다."),
    POST_NOT_OWNER(HttpStatus.BAD_REQUEST, "본인 글만 수정/삭제가 가능합니다."),
    POST_TYPE_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 게시물 타입입니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {

        this.httpStatus = httpStatus;
        this.message = message;
    }
}
