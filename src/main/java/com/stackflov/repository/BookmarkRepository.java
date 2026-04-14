package com.stackflov.repository;

import com.stackflov.domain.Board;
import com.stackflov.domain.Bookmark;
import com.stackflov.domain.Review;
import com.stackflov.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Optional<Bookmark> findByUserAndBoard(User user, Board board);

    List<Bookmark> findByUser(User user);

    List<Bookmark> findByBoard(Board board);

    List<Bookmark> findByUserAndActiveTrue(User user);

    boolean existsByUserAndBoardAndActiveTrue(User user, Board board);

    boolean existsByUserAndReviewAndActiveTrue(User user, Review review);

    Optional<Bookmark> findByUserAndReview(User user, Review review);

    long countByReview(Review review);
}
