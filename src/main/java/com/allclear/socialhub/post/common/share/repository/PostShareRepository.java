package com.allclear.socialhub.post.common.share.repository;

import com.allclear.socialhub.post.common.response.StatisticResponse;
import com.allclear.socialhub.post.common.share.domain.PostShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PostShareRepository extends JpaRepository<PostShare, Long> {

    @Query("SELECT DATE_FORMAT(ps.createdAt, '%Y-%m-%d') AS time, count(*) AS value " +
            "FROM PostShare AS ps " +
            "WHERE ps.post.id in :postIds " +
            "AND DATE(ps.createdAt) BETWEEN :start AND :end " +
            "GROUP BY DATE_FORMAT(ps.createdAt, '%Y-%m-%d') " +
            "ORDER BY time ASC")
    List<StatisticResponse> findStatisticDtoByPostIds(
            @Param("postIds") List<Long> postIds,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

}
