package com.checkit.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class FavoriteCategoryReq {

    @JsonProperty("category_ids")
    @Size(min = 1, max = 3, message = "관심 카테고리는 1개에서 3개 사이로 선택해주세요.")
    private List<String> categoryIds;
}
