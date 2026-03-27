package com.rentloop.repository;

import com.rentloop.entity.BlacklistCandidate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BlacklistCandidateRepository extends JpaRepository<BlacklistCandidate, String> {
    List<BlacklistCandidate> findByStatusOrderByCreatedAtDesc(BlacklistCandidate.CandidateStatus status);
    Optional<BlacklistCandidate> findByUserIdAndStatus(String userId, BlacklistCandidate.CandidateStatus status);
    boolean existsByUserIdAndStatus(String userId, BlacklistCandidate.CandidateStatus status);
    List<BlacklistCandidate> findAllByOrderByCreatedAtDesc();
}
