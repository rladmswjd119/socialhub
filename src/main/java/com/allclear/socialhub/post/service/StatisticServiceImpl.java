package com.allclear.socialhub.post.service;

import com.allclear.socialhub.common.exception.CustomException;
import com.allclear.socialhub.common.exception.ErrorCode;
import com.allclear.socialhub.common.util.DateUtil;
import com.allclear.socialhub.post.common.hashtag.repository.HashtagRepository;
import com.allclear.socialhub.post.common.like.repository.PostLikeRepository;
import com.allclear.socialhub.post.common.response.StatisticQueryResponse;
import com.allclear.socialhub.post.common.share.repository.PostShareRepository;
import com.allclear.socialhub.post.common.view.repository.PostViewRepository;
import com.allclear.socialhub.post.domain.StatisticType;
import com.allclear.socialhub.post.domain.StatisticValue;
import com.allclear.socialhub.post.dto.StatisticResponse;
import com.allclear.socialhub.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticServiceImpl implements StatisticService {

    private final HashtagRepository hashtagRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostShareRepository postShareRepository;
    private final PostViewRepository postViewRepository;

    /**
     * 1. 통계
     * 작성자 : 김효진, 김유현
     *
     * @param hashtag
     * @param type    : 일자별, 시간별
     * @param start   : start date
     * @param end     : end date
     * @param value   : count, like_count, share_count, view_count
     * @return List<StatisticDto>
     */
    @Override
    public List<StatisticResponse> getStatistics(String hashtag, StatisticType type, LocalDate start, LocalDate end, StatisticValue value) {

        // 0. 날짜 검증
        validateDateRange(type, start, end);

        // 1. 일자별 혹은 시간대별 날짜 포맷 패턴 설정
        String queryDateFormatPattern = getQueryDateFormatPattern(type);

        // 2. hashtag 테이블에서 해시태그 가진 게시물 리스트
        List<Long> postIds = hashtagRepository.getPostByHashtag(hashtag);

        // 3. start ~ end 날짜로 일자별 혹은 시간대별 개수를 가져오는 쿼리 날린 결과
        List<StatisticQueryResponse> queryResponses = getQueryResponsesByValue(value, postIds, start, end, queryDateFormatPattern);

        // 4. 시간 - 개수 Map으로 변환
        Map<String, Long> queryResponseMap = convertStatisticQueryResponseToMap(queryResponses);

        // 5. 일자별 혹은 시간대별로 리스트 초기화
        List<StatisticResponse> initializeStatisticsResponses = initializeStatistics(type, start, end);

        // 6. 5에서 초기화한 리스트에 시간 - 개수 Map에 존재하는 시간이면 개수를 설정
        return updateStatisticsWithQueryResults(queryResponseMap, initializeStatisticsResponses);

    }

    /**
     * 1-0. 날짜 범위가 유효한지 검증합니다.
     * - 작성자 : 김유현
     *
     * @param type  통계 타입 (일자별, 시간대별)
     * @param start 시작 날짜
     * @param end   종료 날짜
     * @throws CustomException 날짜 범위 유효성 검증 실패 시 발생
     */
    public void validateDateRange(StatisticType type, LocalDate start, LocalDate end) {

        Long diff = DateUtil.getDateDiff(start, end);
        log.info("start ~ end : " + diff);
        if (diff < 0) {
            throw new CustomException(ErrorCode.STATISTICS_INVALID_DATE_RANGE_START_AFTER_END);
        }

        // 통계 타입에 따라 최대 날짜 범위 초과 여부 검증
        switch (type) {
            case DATE -> {
                if (diff > 30) {
                    throw new CustomException(ErrorCode.STATISTICS_INVALID_DATE_RANGE_TOO_LONG_DATE);
                }
            }
            case HOUR -> {
                if (diff > 7) {
                    throw new CustomException(ErrorCode.STATISTICS_INVALID_DATE_RANGE_TOO_LONG_HOUR);
                }
            }
        }
    }


    /**
     * 1-3. 통계 값에 따라 쿼리 결과를 가져옵니다.
     * 작성자 : 김효진, 김유현
     *
     * @param value                  통계 값 (COUNT, LIKE_COUNT, VIEW_COUNT, SHARE_COUNT)
     * @param postIds                통계 계산에 사용할 게시물 ID 리스트
     * @param start                  통계 집계 시작 날짜
     * @param end                    통계 집계 종료 날짜
     * @param queryDateFormatPattern 날짜 포맷 패턴 (ex. '%Y-%m-%d', '%Y-%m-%d %H:%i')
     * @return List<StatisticQueryResponse> 통계 데이터 리스트
     */
    public List<StatisticQueryResponse> getQueryResponsesByValue(StatisticValue value, List<Long> postIds, LocalDate start, LocalDate end, String queryDateFormatPattern) {

        return switch (value) {
            case COUNT -> postRepository.findStatisticByPostIds(postIds, start, end, queryDateFormatPattern);
            case LIKE_COUNT -> postLikeRepository.findStatisticByPostIds(postIds, start, end, queryDateFormatPattern);
            case VIEW_COUNT -> postViewRepository.findStatisticByPostIds(postIds, start, end, queryDateFormatPattern);
            case SHARE_COUNT -> postShareRepository.findStatisticByPostIds(postIds, start, end, queryDateFormatPattern);
        };
    }

    /**
     * 1-4. 쿼리를 통해 가져온 결과를 시간 - 개수의 Map 으로 변환합니다.
     * 작성자 : 김유현
     *
     * @param queryResponses 쿼리 결과 (StatisticQueryResponse 객체 List)
     * @return Map<String, Long> 시간 - 개수 Map
     */
    public Map<String, Long> convertStatisticQueryResponseToMap(List<StatisticQueryResponse> queryResponses) {

        return queryResponses.stream()
                .collect(Collectors.toMap(
                        StatisticQueryResponse::getTime,
                        StatisticQueryResponse::getValue,
                        (existing, replacement) -> existing
                ));
    }


    /**
     * 1-1. 통계 유형에 따른 날짜 포맷 패턴을 반환합니다.
     * 작성자 : 김유현
     *
     * @param type 통계 유형 (일자별 또는 시간별)
     * @return String 날짜 포맷 패턴
     */
    public String getQueryDateFormatPattern(StatisticType type) {

        return switch (type) {
            case DATE -> "%Y-%m-%d";
            case HOUR -> "%Y-%m-%d %H:%i";
        };
    }

    /**
     * 1-5. 통계 유형에 따라 일자별 또는 시간별 데이터를 초기화합니다.
     * 작성자 : 김유현
     *
     * @param type  통계 유형 (일자별 또는 시간별)
     * @param start 시작 날짜
     * @param end   종료 날짜
     * @return List<StatisticResponse> 초기화된 통계 데이터 리스트
     */
    public List<StatisticResponse> initializeStatistics(StatisticType type, LocalDate start, LocalDate end) {

        return switch (type) {
            case DATE -> initializeDailyStatistics(start, end);
            case HOUR -> initializeHourlyStatistics(start, end);
        };
    }

    /**
     * 1-5-1. 주어진 기간에 대해 일자별 통계를 초기화합니다.
     * 작성자 : 김효진
     *
     * @param start 시작 날짜
     * @param end   종료 날짜
     * @return List<StatisticResponse> 초기화된 일자별 통계 데이터 리스트 ex. [{2024-08-24 : 0}, ...]
     */
    public List<StatisticResponse> initializeDailyStatistics(LocalDate start, LocalDate end) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<StatisticResponse> result = new ArrayList<>();

        List<LocalDate> localDates = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            // LocalDate를 문자열로 변환합니다.
            String formattedDate = date.format(formatter);
            StatisticResponse statisticResponse = new StatisticResponse(formattedDate, 0L);
            result.add(statisticResponse);
        }

        return result;

    }

    /**
     * 1-5-2. 주어진 기간에 대해 시간별 통계를 초기화합니다.
     * 작성자 : 김유현
     *
     * @param start 시작 날짜
     * @param end   종료 날짜
     * @return List<StatisticResponse> 초기화된 시간별 통계 데이터 리스트 ex. [{2024-08-24 00:00 : 0}, ...]
     */
    public List<StatisticResponse> initializeHourlyStatistics(LocalDate start, LocalDate end) {

        // 1. LocalDate를 LocalDateTime으로 변환하여 시작 시간과 끝 시간을 설정
        //    시작 시간: start 날짜의 00:00 (하루의 시작)
        //    끝 시간: end 날짜의 23:00
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(23, 0);

        // 2. 결과를 저장할 리스트를 초기화
        List<StatisticResponse> result = new ArrayList<>();

        // 3. 날짜와 시간의 포맷을 설정
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // 4. 시작 시간부터 끝 시간까지 1시간 단위로 반복
        for (LocalDateTime dateTime = startDateTime; !dateTime.isAfter(endDateTime); dateTime = dateTime.plusHours(1)) {
            // 5. 현재 LocalDateTime 객체를 문자열 형식으로 변환
            //    포맷된 문자열은 "yyyy-MM-dd HH:mm" 형태로, 시간 단위로 구분
            String formattedDateTime = dateTime.format(formatter);

            // 6. 포맷된 문자열과 초기 값 0L을 사용하여 StatisticResponse 객체를 생성
            StatisticResponse statisticResponse = new StatisticResponse(formattedDateTime, 0L);
            result.add(statisticResponse);
        }

        return result;
    }

    /**
     * 1-6. 쿼리 결과에서 가져온 시간-개수 맵을 사용하여 초기화된 통계 리스트를 업데이트합니다.
     * 작성자 : 김유현
     *
     * @param queryResponseMap              쿼리 결과에서 가져온 시간-개수 맵
     * @param initializeStatisticsResponses 초기화된 통계 리스트
     * @return List<StatisticResponse>
     */
    public List<StatisticResponse> updateStatisticsWithQueryResults(Map<String, Long> queryResponseMap, List<StatisticResponse> initializeStatisticsResponses) {

        for (StatisticResponse statisticResponse : initializeStatisticsResponses) {
            String time = statisticResponse.getTime();
            // 쿼리 결과에 존재하면 개수 설정
            if (queryResponseMap.containsKey(time)) {
                statisticResponse.setValue(queryResponseMap.get(time));
            }
        }

        return initializeStatisticsResponses;
    }


}
