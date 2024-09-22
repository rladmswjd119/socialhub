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
    INVALID_USERNAME_LENGTH(HttpStatus.BAD_REQUEST, "계정명은 3자 이상 20자 이하로 설정해야 합니다."),
    INVALID_PASSWORD_LENGTH(HttpStatus.BAD_REQUEST, "비밀번호는 10자 이상 설정해야 합니다."),
    INVALID_PASSWORD_PATTERN(HttpStatus.BAD_REQUEST, "비밀번호는 10자 이상, 20자 이하 영문자, 숫자, 특수문자(!@#$%^&*)중 2가지 이상 포함해야 합니다."),
    PASSWORD_REUSED(HttpStatus.BAD_REQUEST, "기존에 사용한 비밀번호는 재사용할 수 없습니다."),
	
    // POST
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시물입니다."),
    POST_OWNER_MISMATCH(HttpStatus.BAD_REQUEST, "본인 글만 수정/삭제가 가능합니다."),
    POST_TYPE_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 게시물 타입입니다."),
    INVALID_HASHTAG_PATTERN(HttpStatus.BAD_REQUEST, "'#해시태그' 형식만 등록 가능합니다."),
    INVALID_SEARCH_CONDITION(HttpStatus.BAD_REQUEST, "알맞은 검색조건이 아닙니다."),

    // STATISTICS
    STATISTICS_INVALID_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 type parameter 입니다. expected: ['date', 'hour']"),
    STATISTICS_INVALID_VALUE(HttpStatus.BAD_REQUEST,
            "유효하지 않은 value parameter 입니다. expected: ['count', 'view_count', 'like_count', 'share_count']"),
    STATISTICS_INVALID_DATE(HttpStatus.BAD_REQUEST, "유효하지 않은 날짜입니다. expected: 'yyyy-MM-DD'"),
    STATISTICS_INVALID_DATE_RANGE_TOO_LONG_DATE(HttpStatus.BAD_REQUEST, "최대 30일까지만 조회할 수 있습니다."),
    STATISTICS_INVALID_DATE_RANGE_TOO_LONG_HOUR(HttpStatus.BAD_REQUEST, "최대 7일까지만 조회할 수 있습니다."),
    STATISTICS_INVALID_DATE_RANGE_START_AFTER_END(HttpStatus.BAD_REQUEST, "start 날짜는 end 날짜보다 이전이거나 같아야 합니다."),

    // JWT
    INVALID_JWT_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않은 JWT 토큰입니다."),
    JWT_CLAIMS_EMPTY(HttpStatus.BAD_REQUEST, "JWT 클레임이 비어 있습니다."),
    EXPIRED_JWT_TOKEN(HttpStatus.BAD_REQUEST, "만료된 JWT 토큰입니다."),
    EXIST_REFRESH_TOKEN(HttpStatus.CONFLICT, "이미 Refresh Token이 존재합니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {

        this.httpStatus = httpStatus;
        this.message = message;
    }
}
