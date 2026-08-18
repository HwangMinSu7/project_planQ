package com.example.ex76.service;

import com.example.ex76.dto.MovieDTO;
import com.example.ex76.dto.PageRequestDTO;
import com.example.ex76.dto.PageResultDTO;
import com.example.ex76.entity.Movie;
import com.example.ex76.entity.MovieImage;
import com.example.ex76.repository.MovieImageRepository;
import com.example.ex76.repository.MovieRepository;
import com.example.ex76.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URLDecoder;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@Log4j2
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {
  private final MovieRepository movieRepository;
  private final MovieImageRepository movieImageRepository;
  private final ReviewRepository reviewRepository;

  @Value("${com.example.upload.path}")
  private String uploadPath;

  @Override
  public PageResultDTO<MovieDTO, Object[]> getList(PageRequestDTO pageRequestDTO) {
    // 검색하기 위한 객체
    Pageable pageable = pageRequestDTO.getPageable(Sort.by("mno").descending());

    // 1. Page
    //Page<Object[]> result = movieRepository.getListPageMaxMi(pageable);
    Page<Object[]> result = movieRepository.searchPage(
        pageRequestDTO.getType(), pageRequestDTO.getKeyword(), pageable);

    // 2. Function
    Function<Object[], MovieDTO> function = arr -> entitiesToDTO(
        (Movie) arr[0], (List<MovieImage>) (Arrays.asList((MovieImage) arr[1])),
        (Double) arr[2], (Long) arr[3]
    );
    return new PageResultDTO<>(result, function);
  }

  @Override
  public Long register(MovieDTO movieDTO) {
    Map<String, Object> map = dtoToEntity(movieDTO);
    Movie movie = (Movie)map.get("movie");
    List<MovieImage> movieImageList =(List<MovieImage>) map.get("imgList");
    movieRepository.save(movie);
    movieImageList.forEach(image -> movieImageRepository.save(image));
    return movie.getMno();
  }

  @Override
  public MovieDTO get(Long mno) {
    List<Object[]> result = movieRepository.getMovieWithAll(mno);
    Movie movie = (Movie) result.get(0)[0];
    List<MovieImage> movieImageList = new ArrayList<>();
    result.forEach(new Consumer<Object[]>() {
      @Override
      public void accept(Object[] row) {
        MovieImage movieImage = (MovieImage) row[1];
        movieImageList.add(movieImage);
      }
    });

    Double avg = (Double) result.get(0)[2];
    Long reviewCnt = (Long) result.get(0)[3];
    return entitiesToDTO(movie, movieImageList, avg, reviewCnt);
  }

  @Transactional
  @Override
  public void modify(MovieDTO movieDTO) {
    // 넘어온 movieDTO의 mno로 DB로부터 정보를 받아옴
    Optional<Movie> result = movieRepository.findById(movieDTO.getMno());
    // 정보가 있다면
    if (result.isPresent()) {
      // entityMap에는 movie, imgList 가 들어 있음.
      Map<String, Object> entityMap = dtoToEntity(movieDTO);//MovieService로 이동
      // movie 꺼냄
      Movie movie = (Movie) entityMap.get("movie");
      movie.changeTitle(movieDTO.getTitle()); // Movie의 title만 변경
      movieRepository.save(movie); //Movie 저장

      // imgList 꺼내어서 newImageList 변수에 할당(클라이언트에서 온 변경된 이미지)
      List<MovieImage> newImageList = (List<MovieImage>) entityMap.get("imgList");
      // movieImageRepository로 부터 가져온 예전의 이미지 목록
      List<MovieImage> oldImageList = movieImageRepository.findByMno(movie.getMno());

      if(newImageList == null || newImageList.size() == 0) {
        // 수정창에서 이미지 모두를 지웠을 때
        movieImageRepository.deleteByMno(movie.getMno());
        for (int i = 0; i < oldImageList.size(); i++) {
          MovieImage oldMovieImage = oldImageList.get(i);
          if (oldMovieImage.getPath() != null) {
            String fileName = oldMovieImage.getPath() + File.separator
                + oldMovieImage.getUuid() + "_" + oldMovieImage.getImgName();
            deleteFile(fileName);
          }
        }
      } else { // newImageList에 일부 변화 발생
        // 새로운 이미지를 추가하기 위한 forEach
        newImageList.forEach(movieImage -> {
          boolean result1 = false;
          // 변경하고자 하는 newImage를 예전 이미지 목록에서 같은 것이 있는 지를 찾음.
          for (int i = 0; i < oldImageList.size(); i++) {
            //oldImageList에서 uuid를 비교하여 새이미지의 uuid와 같은 것이 있는지 비교
            result1 = oldImageList.get(i).getUuid().equals(movieImage.getUuid());
            if(result1) break; // 찾았을 경우 for문 빠져서 다시 forEach로 돌아감
          }
          if(!result1) movieImageRepository.save(movieImage);
        });
        // 없어진 이미지를 찾아서 삭제하기 위한 forEach문
        oldImageList.forEach(new Consumer<MovieImage>() {
          @Override
          public void accept(MovieImage oldMovieImage) {
            boolean result1 = false;
            for (int i = 0; i < newImageList.size(); i++) {
              result1 = newImageList.get(i).getUuid().equals(oldMovieImage.getUuid());
              if (result1) break;
            }
            // db의 이미지 리스트가 새로 추가된 이미지박스에 내용이 없다면 지우는 부분
            if (!result1) {
              movieImageRepository.deleteByUuid(oldMovieImage.getUuid()); // db에서 지우기
              if (oldMovieImage.getPath() != null) { // 실제 파일 지우기
                String fileName = oldMovieImage.getPath() + File.separator
                    + oldMovieImage.getUuid() + "_" + oldMovieImage.getImgName();
                MovieServiceImpl.this.deleteFile(fileName);
              }
            }
          }
        });
      }
    }
  }

  private void deleteFile(String fileName) {
    // 실제 파일도 지우기
    String searchFilename = null;
    try {
      searchFilename = URLDecoder.decode(fileName, "UTF-8");
      File file = new File(uploadPath + File.separator + searchFilename);
      file.delete();
      new File(file.getParent(), "s_" + file.getName()).delete();
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }


  @Transactional
  @Override
  public void removeWithReviewsAndMovieImages(Long mno) {
    // 영화를 지우면 리뷰, 영화 이미지도 지우는 메서드
    List<MovieImage> list = movieImageRepository.findByMno(mno);
    List<String> result = new ArrayList<>();

    list.forEach(new Consumer<MovieImage>() {
      @Override
      public void accept(MovieImage t) {
        result.add(t.getPath() + File.separator + t.getUuid() + "_" + t.getImgName());
      }
    });
    result.forEach(new Consumer<String>() {
      @Override
      public void accept(String fileName) {
        try {
          log.info("removeFile............" + fileName);
          String srcFileName = URLDecoder.decode(fileName, "UTF-8");
          File file = new File(uploadPath + File.separator + srcFileName);
          file.delete();
          File thumb = new File(file.getParent(), "s_" + file.getName());
          thumb.delete();
        } catch (Exception e) {
          log.info("remove file : " + e.getMessage());
        }
      }
    });

    // 순서 중요
    movieImageRepository.deleteByMno(mno);
    reviewRepository.deleteByMno(mno);
    movieRepository.deleteById(mno);
  }

  @Override
  public void removeMovieImagebyUUID(String uuid) {
    movieImageRepository.deleteByUuid(uuid);
  }
}
