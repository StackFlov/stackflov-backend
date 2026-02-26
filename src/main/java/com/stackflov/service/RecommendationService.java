package com.stackflov.service;

import com.stackflov.domain.EventType;
import com.stackflov.domain.ItemSim;
import com.stackflov.domain.UserReco;
import com.stackflov.repository.ItemSimRepository;
import com.stackflov.repository.UserRecoRepository;
import com.stackflov.repository.UserEventRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class RecommendationService {

    private final EntityManager em;
    private final UserEventRepository userEventRepository;
    private final ItemSimRepository itemSimRepository;
    private final UserRecoRepository userRecoRepository;

    // 튜닝값 (처음엔 하드코딩으로 OK)
    private final int profileDays = 30;
    private final int topUserFeatures = 30;
    private final int candidateFromContent = 300;
    private final int candidateFromCf = 300;
    private final int topSimilarPerSeed = 50;

    private final double wContent = 0.55;
    private final double wCf = 0.45;

    private static final List<EventType> POSITIVE =
            List.of(EventType.LIKE, EventType.BOOKMARK, EventType.CLICK, EventType.DWELL);

    public RecommendationService(
            EntityManager em,
            UserEventRepository userEventRepository,
            ItemSimRepository itemSimRepository,
            UserRecoRepository userRecoRepository
    ) {
        this.em = em;
        this.userEventRepository = userEventRepository;
        this.itemSimRepository = itemSimRepository;
        this.userRecoRepository = userRecoRepository;
    }

    @Transactional
    public void rebuildUserReco(Long userId) {
        // 유저의 최근 positive seed(협업필터링용)
        LocalDateTime since = LocalDateTime.now().minusDays(profileDays);
        List<Long> seedBoards = userEventRepository.findRecentPositiveBoardIds(userId, since, POSITIVE);

        Map<Long, Double> scoreB = generateContentCandidates(userId);
        Map<Long, Double> scoreC = generateCfCandidates(seedBoards);

        Map<Long, Double> finalScore = hybrid(scoreB, scoreC);

        // 저장(기존 삭제 후 상위 N개 캐시)
        userRecoRepository.deleteAllByUserId(userId);

        LocalDateTime now = LocalDateTime.now();
        List<UserReco> rows = finalScore.entrySet().stream()
                .sorted((a,b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(300)
                .map(e -> new UserReco(userId, e.getKey(), e.getValue(), "hybrid", now))
                .toList();

        userRecoRepository.saveAll(rows);
    }

    /**
     * B: user_feature_score 상위 N개를 기준으로 item_feature join해서 후보 점수 뽑기
     * (Native SQL로 빠르게)
     */
    private Map<Long, Double> generateContentCandidates(Long userId) {
        // 상위 feature를 먼저 뽑고(서브쿼리), item_feature와 조인해서 board 후보 점수 합산
        String sql = """
            SELECT f.board_id AS boardId, SUM(ufs.score * f.weight) AS s
            FROM (
                SELECT feature_type, feature_value, score
                FROM user_feature_score
                WHERE user_id = :userId
                ORDER BY score DESC
                LIMIT :topN
            ) ufs
            JOIN item_feature f
              ON f.feature_type = ufs.feature_type
             AND f.feature_value = ufs.feature_value
            GROUP BY f.board_id
            ORDER BY s DESC
            LIMIT :limit
        """;

        Query q = em.createNativeQuery(sql);
        q.setParameter("userId", userId);
        q.setParameter("topN", topUserFeatures);
        q.setParameter("limit", candidateFromContent);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();

        Map<Long, Double> out = new HashMap<>();
        for (Object[] r : rows) {
            Long boardId = ((Number) r[0]).longValue();
            Double s = ((Number) r[1]).doubleValue();
            out.put(boardId, s);
        }
        return out;
    }

    /**
     * C: seedBoards 각각에 대해 item_sim topK를 누적해 후보 점수
     */
    private Map<Long, Double> generateCfCandidates(List<Long> seedBoards) {
        Map<Long, Double> out = new HashMap<>();
        if (seedBoards == null || seedBoards.isEmpty()) return out;

        int seedLimit = Math.min(20, seedBoards.size());
        for (int i = 0; i < seedLimit; i++) {
            Long seed = seedBoards.get(i);
            double seedWeight = 1.0 / (1.0 + i); // 최근일수록 가중

            List<ItemSim> sims = itemSimRepository.findTopSimilar(seed, PageRequest.of(0, topSimilarPerSeed));
            for (ItemSim s : sims) {
                Long cand = s.getId().getBoardB();
                out.merge(cand, s.getSim() * seedWeight, Double::sum);
            }
        }

        // 후보 개수 제한
        return out.entrySet().stream()
                .sorted((a,b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(candidateFromCf)
                .collect(HashMap::new, (m,e)->m.put(e.getKey(), e.getValue()), HashMap::putAll);
    }

    private Map<Long, Double> hybrid(Map<Long, Double> b, Map<Long, Double> c) {
        Map<Long, Double> out = new HashMap<>();

        double maxB = b.values().stream().mapToDouble(x->x).max().orElse(0.0);
        double maxC = c.values().stream().mapToDouble(x->x).max().orElse(0.0);

        Set<Long> keys = new HashSet<>();
        keys.addAll(b.keySet());
        keys.addAll(c.keySet());

        for (Long boardId : keys) {
            double nb = (maxB > 0) ? b.getOrDefault(boardId, 0.0) / maxB : 0.0;
            double nc = (maxC > 0) ? c.getOrDefault(boardId, 0.0) / maxC : 0.0;

            double s = wContent * nb + wCf * nc;
            out.put(boardId, s);
        }
        return out;
    }
}