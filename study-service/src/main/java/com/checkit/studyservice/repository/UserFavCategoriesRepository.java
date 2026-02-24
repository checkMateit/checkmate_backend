package com.checkit.studyservice.repository;

import com.checkit.studyservice.entity.UserFavCategories;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserFavCategoriesRepository extends JpaRepository<UserFavCategories, UUID> {
}
