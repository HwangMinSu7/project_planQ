package com.example.ex76.service;

import com.example.ex76.dto.ReviewDTO;
import com.example.ex76.entity.Movie;
import com.example.ex76.entity.Review;
import com.example.ex76.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
  private final ReviewRepository reviewRepository;

  @Override
  public Long register(ReviewDTO reviewDTO) {
    Review review = dtoToEntity(reviewDTO);
    reviewRepository.save(review);
    return review.getReviewNum();
  }

  @Override
  public List<ReviewDTO> getList(Long mno) {
    List<Review> tmp = reviewRepository.findByMovie(Movie.builder().mno(mno).build());
    List<ReviewDTO> result = tmp.stream().map(review -> entityToDto(review)).collect(Collectors.toList());
    return result;
  }

  @Override
  public Long modify(ReviewDTO reviewDTO) {
    Optional<Review> result = reviewRepository.findById(reviewDTO.getReviewNum());
    if (result.isPresent()) {
      Review review = result.get();
      review.changeText(reviewDTO.getText());
      review.changeGrade(reviewDTO.getGrade());
      reviewRepository.save(review);
      return review.getReviewNum(); // 수정이 잘되면 review번호 리턴
    }
    return 0L; // 변경하고자 하는 것이 없는 경우 0을 리턴
  }

  @Override
  public Long remove(Long reviewNum) {
    try {
      reviewRepository.deleteById(reviewNum);
    } catch (Exception e) {
      return 0L;
    }
    return reviewNum;
  }
}
