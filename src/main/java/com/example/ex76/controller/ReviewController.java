package com.example.ex76.controller;

import com.example.ex76.dto.ReviewDTO;
import com.example.ex76.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Log4j2
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
  private final ReviewService reviewService;

  // /ex6/reviews/100  :: 경로변수 사용
  @GetMapping(value = "/{mno}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<ReviewDTO>> getList(@PathVariable("mno") Long mno) {
    return new ResponseEntity<>(reviewService.getList(mno), HttpStatus.OK);
  }

  @PostMapping({"","/"})
  public ResponseEntity<String> register(@RequestBody ReviewDTO reviewDTO) {
    String result = reviewService.register(reviewDTO) + "번 리뷰 등록";
    return new ResponseEntity<>(result, HttpStatus.OK);
  }
  @PutMapping({"","/"})
  public ResponseEntity<String> modify(@RequestBody ReviewDTO reviewDTO) {
    String result = reviewService.modify(reviewDTO) + "번 리뷰 수정";
    return new ResponseEntity<>(result, HttpStatus.OK);
  }
  @DeleteMapping(value = "/{reviewNum}")
  public ResponseEntity<String> delete(@PathVariable Long reviewNum) {
    String result = reviewService.remove(reviewNum) + "번 리뷰 삭제";
    return new ResponseEntity<>(result, HttpStatus.OK);
  }
}
