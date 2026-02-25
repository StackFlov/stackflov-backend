package com.stackflov.service;

import com.stackflov.domain.EventType;
import com.stackflov.domain.ItemSim;
import com.stackflov.repository.ItemSimRepository;
import com.stackflov.repository.UserEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ItemSimilarityService {

    private final UserEventRepository userEventRepository;
    private final ItemSimRepository itemSimRepository;

    // 튜닝값
    private final int simDays = 30;
    private final int perUserMaxBoards = 50;     // 유저당 최대 시드 개수(비용 제한)
    private final int topSimilarPerBoard = 50;   // boardA 당 boardB 상위 몇 개 저장

    private static final List<EventType> POSITIVE =
            List.of(EventType.LIKE, EventType.BOOKMARK, EventType.CLICK, EventType.DWELL);

    public ItemSimilarityService(UserEventRepository userEventRepository, ItemSimRepository itemSimRepository) {
        this.userEventRepository = userEventRepository;
        this.itemSimRepository = itemSimRepository;
    }

    /**
     * 활성 유저들의 positive co-occurrence로 item_sim 재생성
     */
    @Transactional
    public void rebuildItemSimForActiveUsers() {
        LocalDateTime sinceActive = LocalDateTime.now().minusDays(7);
        List<Long> activeUsers = userEventRepository.findActiveUserIds(sinceActive);
        if (activeUsers.isEmpty()) return;

        LocalDateTime since = LocalDateTime.now().minusDays(simDays);

        Map<Long, Integer> boardCnt = new HashMap<>(); // cnt(board): positive한 유저 수
        Map<Long, Map<Long, Integer>> co = new HashMap<>(); // co[a][b] = 함께 본 횟수

        for (Long userId : activeUsers) {
            List<Long> boards = userEventRepository.findRecentPositiveBoardIds(userId, since, POSITIVE);
            if (boards.isEmpty()) continue;

            if (boards.size() > perUserMaxBoards) boards = boards.subList(0, perUserMaxBoards);

            // 중복 제거(순서 유지)
            List<Long> list = new ArrayList<>(new LinkedHashSet<>(boards));

            for (Long a : list) boardCnt.merge(a, 1, Integer::sum);

            // O(k^2) (k <= 50이라 MVP에선 OK)
            for (int i = 0; i < list.size(); i++) {
                for (int j = 0; j < list.size(); j++) {
                    if (i == j) continue;
                    Long a = list.get(i);
                    Long b = list.get(j);
                    co.computeIfAbsent(a, k -> new HashMap<>()).merge(b, 1, Integer::sum);
                }
            }
        }

        LocalDateTime now = LocalDateTime.now();

        // boardA 별로 상위 topSimilarPerBoard만 저장
        for (Map.Entry<Long, Map<Long, Integer>> entry : co.entrySet()) {
            Long a = entry.getKey();
            Map<Long, Integer> map = entry.getValue();

            int cntA = boardCnt.getOrDefault(a, 1);

            // 기존 a 삭제 후 재생성
            itemSimRepository.deleteByBoardA(a);

            List<ItemSim> sims = map.entrySet().stream()
                    .map(en -> {
                        Long b = en.getKey();
                        int coAB = en.getValue();
                        int cntB = boardCnt.getOrDefault(b, 1);
                        double sim = coAB / Math.sqrt((double) cntA * cntB); // cosine 형태
                        return new AbstractMap.SimpleEntry<>(b, sim);
                    })
                    .sorted((x, y) -> Double.compare(y.getValue(), x.getValue()))
                    .limit(topSimilarPerBoard)
                    .map(en -> new ItemSim(a, en.getKey(), en.getValue(), now))
                    .toList();

            itemSimRepository.saveAll(sims);
        }
    }
}