package com.example.ex76.repository;

import com.example.ex76.entity.Member;
import com.example.ex76.entity.Movie;
import com.example.ex76.entity.Review;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
  // 해당 영화로 관련 리뷰만 추출
  // EntityGraph::해당엔티티의 ManyToOne으로 member를 가져올수 없으나 필요할 때 지정해서 가져오려고 사용
  @EntityGraph(attributePaths = {"member"}, type = EntityGraph.EntityGraphType.FETCH)
  List<Review> findByMovie(Movie movie);

  // 회원 삭제시 관련 review 모두 삭제
  @Modifying // update or delete 할 경우 반드시 필요
  @Query("delete from Review r where r.member = :member ")
  void deleteByMember(Member member);

  @Modifying // update or delete 할 경우 반드시 필요
  @Query("delete from Review r where r.movie.mno = :mno ")
  void deleteByMno(@Param("mno")Long mno);

}
