package com.allclear.socialhub.user.controller;

import org.springframework.http.HttpHeaders;

import com.allclear.socialhub.auth.dto.UserDetailsImpl;
import com.allclear.socialhub.common.exception.CustomException;
import com.allclear.socialhub.auth.util.AccessTokenUtil;
import com.allclear.socialhub.user.dto.*;
import com.allclear.socialhub.user.service.EmailService;
import com.allclear.socialhub.user.service.UserService;
import com.allclear.socialhub.user.type.EmailType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "User", description = "사용자 API")
public class UserController {

    private final EmailService emailService;
    private final UserService userService;
    private final AccessTokenUtil accessTokenUtil;

    @Operation(summary = "이메일 인증 코드 전송", description = "사용자의 이메일로 인증 코드를 전송합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증 코드가 이메일로 전송됨",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "이메일 전송 실패",
                    content = @Content)
    })
    @PostMapping("/email-code")
    public ResponseEntity<String> sendEmailVerification(@AuthenticationPrincipal UserDetailsImpl userDetails) {

        try {
            String email = userDetails.getEmail();
            emailService.sendEmail(email, EmailType.VERIFICATION);
            return ResponseEntity.status(HttpStatus.OK).body("이메일로 인증 코드가 전송되었습니다.");

        } catch (MessagingException e) {
            // 이메일 전송 실패 시 500 응답
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이메일 전송 실패");
        }
    }

    @Operation(summary = "이메일 인증 코드 확인", description = "사용자가 입력한 인증 코드를 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이메일 인증 성공",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "인증 코드가 일치하지 않거나 만료됨",
                    content = @Content)
    })
    @PostMapping("/email-verify")
    public ResponseEntity<String> verifyEmailCode(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                  @Valid @RequestBody UserEmailRequest request) {

        String email = userDetails.getEmail();
        String storedCode = emailService.getVerificationToken(email);

        if (userService.verifyUser(storedCode, request.getAuthCode(), email)) {
            return ResponseEntity.status(HttpStatus.OK).body("이메일 인증이 완료되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증 코드가 일치하지 않거나 만료되었습니다.");
        }
    }

    @Operation(summary = "회원 가입", description = "사용자가 회원 가입을 요청합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원 가입 성공",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content)
    })
    @PostMapping("")
    public ResponseEntity<String> joinUser(@Valid @RequestBody UserJoinRequest request) {

        try {
            userService.joinUser(request);
            System.out.println(request.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 완료되었습니다.");
        } catch (CustomException e) {
            throw e;
        }
    }

    @Operation(summary = "로그인", description = "사용자가 로그인을 요청합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/login")
    public ResponseEntity<String> userLogin(@Valid @RequestBody UserLoginRequest request) {

        HttpHeaders httpHeaders = userService.userLogin(request);

        return ResponseEntity.ok().headers(httpHeaders).body("로그인이 완료되었습니다.");
    }

    @Operation(summary = "계정 중복 체크", description = "사용자가 계정 중복 체크를 요청합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "해당 계정은 사용이 가능합니다.",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "해당 계정은 이미 사용중입니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/duplicate-check")
    public ResponseEntity<String> userDuplicateCheck(@RequestBody String username) {

        String message = userService.userDuplicateCheck(username);
        return ResponseEntity.ok(message);
    }

    @PatchMapping("")
    @Operation(summary = "회원정보 수정", description = "회원의 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 회원정보가 수정되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다.")
    })
    public ResponseEntity<UserInfoUpdateResponse> updateUserInfo(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody UserInfoUpdateRequest request) {

        UserInfoUpdateResponse response = userService.updateUserInfo(request, userDetails);

        return ResponseEntity.status(200).body(response);
    }

}
