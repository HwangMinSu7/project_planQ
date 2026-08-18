package com.example.ex76.repository;

import com.example.ex76.entity.Member;
import com.example.ex76.entity.Movie;
import com.example.ex76.entity.Review;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
class ReviewRepositoryTests {

  @Autowired
  ReviewRepository reviewRepository;

  @Autowired
  MemberRepository memberRepository;

  @Autowired
  MovieRepository movieRepository;

  @Test
  @Disabled("기존 수업 DB의 review 테이블에 제거되지 않은 member_v2 외래키가 남아 있어 수동 정리 후 실행")
  @Transactional
  public void insertMovieReview() {
    Member member = memberRepository.save(Member.builder()
        .email("review-test@onequest.local").pw("1").nickname("tester").build());
    Movie movie = movieRepository.save(Movie.builder().title("Review test movie").build());
    Review review = Review.builder()
        .member(member).movie(movie).grade(5).text("Good Movie").build();
    reviewRepository.saveAndFlush(review);
  }

  @Test
  public void testFindByMovie() {
    List<Review> result = reviewRepository.findByMovie(
        Movie.builder().mno(94L).build());
    result.forEach(review -> {
      System.out.println(review.getReviewNum());
      System.out.println(review.getText());
      System.out.println(review.getGrade());
      System.out.println(review.getMember().getEmail());
      System.out.println("===============================");
    });
  }
}
