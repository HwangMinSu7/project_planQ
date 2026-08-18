package com.example.ex76.service;

import com.example.ex76.dto.MovieDTO;
import com.example.ex76.dto.MovieImageDTO;
import com.example.ex76.dto.PageRequestDTO;
import com.example.ex76.dto.PageResultDTO;
import com.example.ex76.entity.Movie;
import com.example.ex76.entity.MovieImage;

import java.util.*;
import java.util.stream.Collectors;

public interface MovieService {
  Long register(MovieDTO movieDTO);
  PageResultDTO<MovieDTO, Object[]> getList(PageRequestDTO pageRequestDTO);
  MovieDTO get(Long mno);
  void modify(MovieDTO movieDTO);
  void removeWithReviewsAndMovieImages(Long mno); // mno로 관련 내용 모두 삭제
  void removeMovieImagebyUUID(String uuid); // uuid로 movieImage 삭제

  default Map<String, Object> dtoToEntity(MovieDTO movieDTO) {
    // movie, imgList라는 키를 가질 수 있도록 선언
    Map<String, Object> map = new HashMap<>();
    Movie movie = Movie.builder()
        .mno(movieDTO.getMno())
        .title(movieDTO.getTitle())
        .build();
    map.put("movie", movie);
    List<MovieImageDTO> imageDTOList = movieDTO.getImageDTOList();

    // imageDTOList에 이미지가 있다면
    if (imageDTOList != null && imageDTOList.size() > 0) {
      List<MovieImage> movieImageList = imageDTOList.stream().map(
          movieImageDTO -> MovieImage.builder()
              .path(movieImageDTO.getPath())
              .imgName(movieImageDTO.getImgName())
              .uuid(movieImageDTO.getUuid())
              .movie(movie)
              .build()
      ).collect(Collectors.toList());
      map.put("imgList", movieImageList);
    }
    return map;
  }

  default MovieDTO entitiesToDTO(Movie movie, List<MovieImage> movieImages, Double avg, Long reviewCnt) {
    MovieDTO movieDTO = MovieDTO.builder()
        .mno(movie.getMno())
        .title(movie.getTitle())
        .regDate(movie.getRegDate())
        .modDate(movie.getModDate())
        .build();
    List<MovieImageDTO> movieImageDTOList =
        (movieImages == null || movieImages.isEmpty()) ? new ArrayList<>() :
            movieImages.stream().filter(Objects::nonNull)
            .map(
                movieImage -> MovieImageDTO.builder()
                              .path(movieImage.getPath())
                              .imgName(movieImage.getImgName())
                              .uuid(movieImage.getUuid())
                              .build()
            ).collect(Collectors.toList());
    movieDTO.setImageDTOList(movieImageDTOList);
    movieDTO.setAvg(avg);
    movieDTO.setReviewCnt(reviewCnt.intValue());
    return movieDTO;
  }
}
