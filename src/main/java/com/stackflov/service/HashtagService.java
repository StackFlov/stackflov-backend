package com.stackflov.service;

import com.stackflov.domain.Board;
import com.stackflov.domain.BoardHashtag;
import com.stackflov.domain.Hashtag;
import com.stackflov.repository.BoardHashtagRepository;
import com.stackflov.repository.HashtagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class HashtagService {

    private final HashtagRepository hashtagRepository;
    private final BoardHashtagRepository boardHashtagRepository;

    // #다음에 한글,영문,숫자,_ 가 오는 패턴을 찾는 정규표현식
    private static final Pattern HASHTAG_PATTERN = Pattern.compile("#([a-zA-Z0-9ㄱ-ㅎㅏ-ㅣ가-힣_]+)");

    @Transactional
    public void processHashtags(String content, Board board) {
        // 1. 게시글 수정 시, 기존의 해시태그 연결을 모두 삭제합니다.
        boardHashtagRepository.deleteAllByBoard(board);

        if (content == null || content.isBlank()) {
            return;
        }

        // 2. 본문에서 해시태그들을 추출합니다.
        Set<String> tagNames = new HashSet<>();
        Matcher matcher = HASHTAG_PATTERN.matcher(content);
        while (matcher.find()) {
            tagNames.add(matcher.group(1));
        }

        // 3. 각 해시태그를 처리합니다.
        for (String name : tagNames) {
            // DB에 이미 태그가 존재하면 그것을 사용하고, 없으면 새로 저장합니다.
            Hashtag hashtag = hashtagRepository.findByName(name)
                    .orElseGet(() -> hashtagRepository.save(Hashtag.builder().name(name).build()));

            // 게시글과 해시태그를 중간 테이블(BoardHashtag)을 통해 연결합니다.
            BoardHashtag boardHashtag = BoardHashtag.builder().board(board).hashtag(hashtag).build();
            boardHashtagRepository.save(boardHashtag);
        }
    }
}