package com.stackflov.repository;

import com.stackflov.domain.BannedWord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BannedWordRepository extends JpaRepository<BannedWord, Long> {
    boolean existsByWord(String word);
    Optional<BannedWord> findByWord(String word);
}