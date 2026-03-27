package com.rentloop.repository;

import com.rentloop.entity.RatingAnalysisLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.Optional;

public interface RatingAnalysisLogRepository extends JpaRepository<RatingAnalysisLog, String> {
    Optional<RatingAnalysisLog> findTopByUserIdOrderByAnalyzedAtDesc(String userId);

    @Query("SELECT MAX(l.analyzedAt) FROM RatingAnalysisLog l WHERE l.userId = :userId")
    Optional<LocalDateTime> findLastAnalyzedAt(String userId);
}
