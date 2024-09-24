## (1) 문제 상황
> 로그인 통합 테스트 실행 중 `username`과 `password`를 `MultiValueMap`에 저장해 컨트롤러로 로그인을 요청하던 중 발생 

## (2) 발생한 문제(에러)
- ⓐ 최초 `💥NullPointerException`이 발생하면서 <u>"가입되지 않은 아이디"</u>라는 사용자 정의 에러 메시지가 전달됨 <p></p>
- ⓑ `@BeforeEach` 애노테이션을 이용해 로그인 테스트 직전에 회원가입을 실행했기 때문에 전달된 ResponseEntity의 HttpStatus를 확인해봤는데, `💥415 UNSUPPORTED_MEDIA_TYPE` 오류가 발생

## (3) 원인
- 추정되는 원인
  - `MultiValueMap`에 담긴 데이터가 파라미터와 바인딩되지 않아서 발생하는 오류라고 생각
    <p></p>
- 실제 원인 
  - `MultiValueMap`은 기본적으로 데이터를 `application/x-www-form-urlencoded` 타입으로 받아서 인코딩 후 JSON으로 변환한다. 
  - 그런데, JSON 형식의 데이터를 보내서 415 UNSUPPORTED_MEDIA_TYPE 에러가 발생한 것 같다.

## (4) 해결방법
> `MultiValueMap` 대신 파라미터와 같은 객체를 사용

## (5) 참고자료
- [x-www-form-urlencoded 타입이란: multipart/form-data와의 차이점](https://wildeveloperetrain.tistory.com/304)
- [[Spring boot] Post 요청, Content-Type](https://velog.io/@hyerin_story/Spring-boot-Post-요청-Content-Type)
