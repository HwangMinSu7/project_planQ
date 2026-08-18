package com.example.ex76.repository;


import com.example.ex76.entity.Movie;
import com.example.ex76.repository.search.SearchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long>, SearchRepository {

  // movie, 평점평균, review갯수 :: 3개
  @Query("select m, avg(coalesce(r.grade, 0)), count(distinct r) " +
      "from Movie m left outer join Review r on r.movie=m group by m ")
  Page<Object[]> getListPage(Pageable pageable);

  // movie, 이미지, 평점평균, review 갯수 :: 4개 => 문제점::이미지 만큼 무비도 복수출력
  @Query("select m, mi, avg(coalesce(r.grade, 0)), count(r) " +
      "from Movie m left outer join MovieImage mi on mi.movie = m " +
      "left outer join Review r on r.movie = m group by m, mi ")
  Page<Object[]> getListPage2(Pageable pageable);

  // movie, 이미지, 평점평균, review 갯수 :: 4개
  // movieImage를 가져오되 그중 하나(max)를 들고오며 군집속성을 만족하도록 group by 추가
  // MariaDB
  // @Query("select m, mi, avg(coalesce(r.grade,0)), count(distinct r) " +
  //    "from Movie m left outer join MovieImage mi on mi.movie = m " +
  //    "and mi.inum = (select max(mi2.inum) from MovieImage mi2 where mi2.movie = m) " +
  //    "left outer join Review r on r.movie = m  " +
  //    "group by m, mi ")

  // MySQL
  @Query("""
    select m, mi, coalesce(avg(r.grade), 0), count(distinct r)
    from Movie m left join MovieImage mi on mi.movie = m left join Review r on r.movie = m
    where mi.inum = (select max(mi2.inum) from MovieImage mi2 where mi2.movie = m)
    group by m, mi
    order by m.mno desc
    """)
  Page<Object[]> getListPageMaxMi(Pageable pageable);

  // 상세보기 페이지 전용 메서드
  @Query("select m, mi, avg(coalesce(r.grade,0)), count(r) " +
      "from Movie m left outer join MovieImage mi on mi.movie = m  " +
      "left outer join Review r on r.movie = m " +
      "where m.mno=:mno group by mi ")
  List<Object[]> getMovieWithAll(Long mno);

}
