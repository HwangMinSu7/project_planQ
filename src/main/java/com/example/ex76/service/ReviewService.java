package com.example.ex76.service;

import com.example.ex76.dto.ReviewDTO;
import com.example.ex76.entity.Member;
import com.example.ex76.entity.Movie;
import com.example.ex76.entity.Review;

import java.util.List;

public interface ReviewService {
  Long register(ReviewDTO reviewDTO);

  List<ReviewDTO> getList(Long mno);

  Long modify(ReviewDTO reviewDTO);

  Long remove(Long reviewNum);

  default Review dtoToEntity(ReviewDTO reviewDTO) {
    Review review = Review.builder()
        .reviewNum(reviewDTO.getReviewNum())
        .movie(Movie.builder().mno(reviewDTO.getMno()).build())
        .member(Member.builder().mid(reviewDTO.getMid()).build())
        .grade(reviewDTO.getGrade())
        .text(reviewDTO.getText())
        .build();
    return review;
  }


  default ReviewDTO entityToDto(Review review) {
    ReviewDTO reviewDTO = ReviewDTO.builder()
        .reviewNum(review.getReviewNum())
        .mno(review.getMovie().getMno())
        .mid(review.getMember().getMid())
        .nickname(review.getMember().getNickname())
        .email(review.getMember().getEmail())
        .grade(review.getGrade())
        .text(review.getText())
        .regDate(review.getRegDate())
        .modDate(review.getModDate())
        .build();
    return reviewDTO;
  }
}
