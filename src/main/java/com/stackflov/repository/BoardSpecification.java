package com.stackflov.repository;

import com.stackflov.domain.Board;
import com.stackflov.domain.User; // 👈 User 엔티티 import 추가
import com.stackflov.dto.BoardSearchConditionDto;
import jakarta.persistence.criteria.Join; // 👈 Join import 추가
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

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
                // 👇 이 부분을 수정합니다.
                // 'author' 필드를 기준으로 User 엔티티와 Join 합니다.
                Join<Board, User> authorJoin = root.join("author");
                // Join한 테이블의 nickname 필드를 기준으로 like 검색을 합니다.
                predicates.add(criteriaBuilder.like(authorJoin.get("nickname"), "%" + condition.getNickname() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}