package com.allclear.socialhub.user.service;

import com.allclear.socialhub.common.exception.CustomException;
import com.allclear.socialhub.common.exception.ErrorCode;
import com.allclear.socialhub.user.domain.User;
import com.allclear.socialhub.user.dto.UserInfoUpdateRequest;
import com.allclear.socialhub.user.dto.UserInfoUpdateResponse;
import com.allclear.socialhub.user.dto.UserJoinRequest;
import com.allclear.socialhub.user.exception.DuplicateUserInfoException;
import com.allclear.socialhub.user.repository.UserRepository;
import com.allclear.socialhub.user.type.UserCertifyStatus;
import com.allclear.socialhub.user.type.UserStatus;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    //private final BCryptPasswordEncoder passwordEncoder; TODO: jwt 기능 구현되면 주석 해제

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

        //String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword()) //TODO: jwt 기능 구현되면 주석해제, encodedPassword로 사용 예정
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

    /**
     * 사용자 계정명의 길이를 검사합니다.
     * 작성자: 배서진
     *
     * @param username
     */
    private void validateUsername(String username) {

        if (username.length() < 3 || username.length() > 20) {
            throw new CustomException(ErrorCode.INVALID_USERNAME_LENGTH);
        }
    }

    /**
     * @param request 사용자 회원정보 수정한 데이터
     * @param email   JWT token에서 추출한 이메일
     * @return Response 객체
     */
    @Override
    public UserInfoUpdateResponse updateUserInfo(UserInfoUpdateRequest request, String email) {
        // 1. 이메일을 사용해 사용자 검색
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_EXIST));

        // 2. 사용자 입력 정보 유효성 검사
        validateUsername(request.getUsername());
        validatePassword(request.getPassword());

        // TODO: 변경 예정
        //String encodedPassword = passwordEncoder.encode(request.getPassword());


        // 3. 사용자 정보 업데이트
        User updateUser = User.builder()
                .username(request.getUsername())
                .password(request.getPassword()) // TODO: encodedPassword
                .build();

        // 4. 업데이트된 사용자 정보 저장
        userRepository.save(updateUser);

        // 5. Response 객체를 생성하여 반환
        return UserInfoUpdateResponse.builder()
                .username(request.getUsername())
                .build();

    }

}
