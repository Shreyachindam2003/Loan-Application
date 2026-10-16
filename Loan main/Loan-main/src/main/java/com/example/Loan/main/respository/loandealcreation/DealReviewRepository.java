package com.example.Loan.main.respository.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.response.DealReviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DealReviewRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<DealReviewResponse> REVIEW_MAPPER =
            (rs, rowNumber) -> {
                Timestamp reviewedAt = rs.getTimestamp("ReviewedAt");

                return new DealReviewResponse(
                        rs.getInt("DealId"),
                        rs.getInt("OfficerId"),
                        rs.getString("Status"),
                        rs.getString("Remarks"),
                        reviewedAt == null
                                ? null
                                : reviewedAt.toLocalDateTime()
                );
            };

    public Optional<DealReviewResponse> findLatestByDealId(
            Integer dealId
    ) {
        List<DealReviewResponse> reviews = jdbc.query("""
                SELECT DealId, OfficerId, Status, Remarks, ReviewedAt
                FROM DealReviews
                WHERE DealId = ?
                ORDER BY ReviewedAt DESC, OfficerId DESC
                LIMIT 1
                """,
                REVIEW_MAPPER,
                dealId
        );

        return reviews.stream().findFirst();
    }

    public Page<DealReviewResponse> findByDealId(
            Integer dealId,
            Pageable pageable
    ) {
        Long total = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM DealReviews
                WHERE DealId = ?
                """,
                Long.class,
                dealId
        );

        List<DealReviewResponse> reviews = jdbc.query("""
                SELECT DealId, OfficerId, Status, Remarks, ReviewedAt
                FROM DealReviews
                WHERE DealId = ?
                ORDER BY ReviewedAt DESC, OfficerId DESC
                LIMIT ? OFFSET ?
                """,
                REVIEW_MAPPER,
                dealId,
                pageable.getPageSize(),
                pageable.getOffset()
        );

        return new PageImpl<>(
                reviews,
                pageable,
                total == null ? 0L : total
        );
    }

    public Page<DealReviewResponse> findByOfficerId(
            Integer officerId,
            Pageable pageable
    ) {
        Long total = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM DealReviews
                WHERE OfficerId = ?
                """,
                Long.class,
                officerId
        );

        List<DealReviewResponse> reviews = jdbc.query("""
                SELECT DealId, OfficerId, Status, Remarks, ReviewedAt
                FROM DealReviews
                WHERE OfficerId = ?
                ORDER BY ReviewedAt DESC, DealId DESC
                LIMIT ? OFFSET ?
                """,
                REVIEW_MAPPER,
                officerId,
                pageable.getPageSize(),
                pageable.getOffset()
        );

        return new PageImpl<>(
                reviews,
                pageable,
                total == null ? 0L : total
        );
    }
}