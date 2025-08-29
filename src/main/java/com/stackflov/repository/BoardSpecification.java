package com.stackflov.repository;

import com.stackflov.domain.Board;
import com.stackflov.dto.BoardSearchConditionDto;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class BoardSpecification {

    public static Specification<Board> search(BoardSearchConditionDto condition) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(condition.getTitle())) {
                predicates.add(criteriaBuilder.like(root.get("title"), "%" + condition.getTitle() + "%"));
            }
            if (StringUtils.hasText(condition.getContent())) {
                predicates.add(criteriaBuilder.like(root.get("content"), "%" + condition.getContent() + "%"));
            }
            if (StringUtils.hasText(condition.getNickname())) {
                // User(작성자)와 조인하여 닉네임으로 검색
                predicates.add(criteriaBuilder.like(root.get("user").get("nickname"), "%" + condition.getNickname() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}