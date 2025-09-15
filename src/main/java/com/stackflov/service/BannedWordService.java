package com.stackflov.service;

import com.stackflov.domain.BannedWord;
import com.stackflov.repository.BannedWordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BannedWordService {

    private final BannedWordRepository bannedWordRepository;

    // 관리자: 금칙어 추가
    @Transactional
    public BannedWord addBannedWord(String word) {
        String trimmedWord = word.trim();
        if (bannedWordRepository.existsByWord(trimmedWord)) {
            throw new IllegalArgumentException("이미 등록된 금칙어입니다.");
        }
        BannedWord bannedWord = BannedWord.builder().word(trimmedWord).build();
        return bannedWordRepository.save(bannedWord);
    }

    // 관리자: 금칙어 삭제
    @Transactional
    public void deleteBannedWord(String word) {
        BannedWord bannedWord = bannedWordRepository.findByWord(word.trim())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 금칙어입니다."));
        bannedWordRepository.delete(bannedWord);
    }

    // 관리자: 모든 금칙어 조회
    @Transactional(readOnly = true)
    public List<BannedWord> getAllBannedWords() {
        return bannedWordRepository.findAll();
    }

    // 시스템: 텍스트에 금칙어가 포함되어 있는지 검사
    @Transactional(readOnly = true)
    public boolean containsBannedWord(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        // DB에서 모든 금칙어를 가져와서 하나라도 포함되는지 확인
        List<BannedWord> bannedWords = getAllBannedWords();
        String lowerCaseText = text.toLowerCase(); // 대소문자 구분 없이 검사

        return bannedWords.stream()
                .anyMatch(bannedWord -> lowerCaseText.contains(bannedWord.getWord().toLowerCase()));
    }
}