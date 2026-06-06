package com.rentloop.repository;

import com.rentloop.entity.Announcement;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AnnouncementRepository extends MongoRepository<Announcement, String> {
    List<Announcement> findAllByOrderByCreatedAtDesc();
}
