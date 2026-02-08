package com.checkit.studyservice.repository;

import com.checkit.studyservice.entity.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
    Optional<Hashtag> findByNormalizedName(String normalizedName);
}
