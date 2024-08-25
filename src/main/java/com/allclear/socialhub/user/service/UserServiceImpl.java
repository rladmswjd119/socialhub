package com.allclear.socialhub.user.service;

import com.allclear.socialhub.common.config.WebSecurityConfig;
import com.allclear.socialhub.common.exception.CustomException;
import com.allclear.socialhub.common.exception.ErrorCode;
import com.allclear.socialhub.user.domain.User;
import com.allclear.socialhub.user.dto.UserJoinRequest;
import com.allclear.socialhub.user.dto.UserLoginRequest;
import com.allclear.socialhub.user.exception.DuplicateUserInfoException;
import com.allclear.socialhub.user.repository.EmailRedisRepository;
import com.allclear.socialhub.user.repository.UserRepository;
import com.allclear.socialhub.user.type.UserCertifyStatus;
import com.allclear.socialhub.user.type.UserStatus;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EmailRedisRepository emailRedisRepository;
    private final WebSecurityConfig securityConfig;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 회원가입
     * 작성자: 배서진
     *
     * @param request 사용자 회원가입 요청 데이터(이메일, 계정명, 비밀번호)
     * @throws DuplicateUserInfoException 이메일 또는 계정명 이미 존재하는 경우
     * @throws CustomException            비밀번호 규칙에 맞지 않을 경우
     */
    @Override
    public void joinUser(UserJoinRequest request) {

        validateUsername(request.getUsername());
        validatePassword(request.getPassword());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserInfoException(ErrorCode.EMAIL_DUPLICATION);
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUserInfoException(ErrorCode.USERNAME_DUPLICATION);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(encodedPassword)
                .status(UserStatus.ACTIVE)
                .certifyStatus(UserCertifyStatus.UNAUTHENTICATED)
                .build();

        userRepository.save(user);

    }

    /**
     * 비밀번호 유효성 검사 메서드
     * 작성자: 배서진
     *
     * @param password 1) 비밀번호 길이 검사
     *                 2) 숫자, 문자, 특수문자 중 두 가지 이상 포함 확인
     */
    private void validatePassword(String password) {

        if (password.length() < 10 || password.length() > 20) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD_LENGTH);
        }

        int criteriaMet = 0;
        if (password.matches(".*[a-zA-Z]+.*")) { // 문자
            criteriaMet++;
        }
        if (password.matches(".*\\d+.*")) { // 숫자
            criteriaMet++;
        }
        if (password.matches(".*[!@#$%^&*()_+=-]+.*")) { // 특수문자
            criteriaMet++;
        }

        if (criteriaMet < 2) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD_PATTERN);
        }

    }

    // 계정명 길이 검증
    private void validateUsername(String username) {

        if (username.length() < 3 || username.length() > 20) {
            throw new CustomException(ErrorCode.INVALID_USERNAME_LENGTH);
        }
    }

    /**
     * 사용자가 제공한 인증 코드(requestCode)와 저장된 인증 코드(storedCode)를 검증하는 메서드.
     * 인증 코드가 일치할 경우, 인증 코드를 삭제하고 검증 성공을 나타내는 true를 반환합니다.
     * 작성자: 배서진
     *
     * @param storedCode  저장된 인증 코드
     * @param requestCode 사용자가 제공한 인증 코드
     * @param email       사용자 이메일
     * @return 코드가 일치하면 true, 그렇지 않으면 false를 반환
     */
    @Override
    public boolean verifyUser(String storedCode, String requestCode, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_EXIST));

        if (storedCode != null && storedCode.equals(requestCode)) {
            user.authenticateUser(); // 인증 상태로 변경
            userRepository.save(user);
            emailRedisRepository.deleteVerificationToken(requestCode);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 로그인
     * 작성자 : 김은정
     *
     * @param request
     */
    public User userLogin(UserLoginRequest request) {

        try {
            User user = checkUsername(request.getUsername());
            checkPassword(user, request.getPassword());

            return user;
        } catch (RuntimeException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * 아이디 확인
     * 작성자 : 김은정
     *
     * @param username
     * @return User user
     */
    public User checkUsername(String username) {

        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new RuntimeException(ErrorCode.USER_NOT_EXIST.getMessage());
        }

        return user;
    }

    /**
     * 비밀번호 확인
     * 작성자 : 김은정
     *
     * @param user
     * @param password
     */
    public void checkPassword(User user, String password) {

        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException(ErrorCode.PASSWORD_NOT_VALID.getMessage());
        }
    }

}
