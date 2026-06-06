package com.rentloop.repository;

import com.rentloop.entity.RatingAnalysisLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;
import java.util.Optional;

public interface RatingAnalysisLogRepository extends MongoRepository<RatingAnalysisLog, String> {
    Optional<RatingAnalysisLog> findTopByUserIdOrderByAnalyzedAtDesc(String userId);

}
