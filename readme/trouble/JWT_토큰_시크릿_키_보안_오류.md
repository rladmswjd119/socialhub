## 1. 문제 상황
- JWT 토큰 발행을 위한 Secret Key 설정 중 발생 

## 2. 발생한 문제(에러)
>해당 에러 발생 : `io.jsonwebtoken.security.WeakKeyException`

## 3. 원인
- 추정되는 원인
  - 오류 제목을 보고 보안이 약한 Secret Key를 설정했기 때문이라고 생각
- 실제 원인
  - Secret Key를 256bit 미만으로 설정했기 때문에 발생

## 4. 해결방법
- Secret Key를 좀 더 길게 설정

## (5) 참고자료
- [Spring Security - io.jsonwebtoken.security.WeakKeyException 원인과 해결 방법](https://green-bin.tistory.com/49)
