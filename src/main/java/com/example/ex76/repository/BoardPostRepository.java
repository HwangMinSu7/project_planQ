package com.example.ex76.repository;

import com.example.ex76.entity.BoardCategory;
import com.example.ex76.entity.BoardPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardPostRepository extends JpaRepository<BoardPost, Long> {
  @EntityGraph(attributePaths = "author")
  Page<BoardPost> findAllByOrderByIdDesc(Pageable pageable);

  @EntityGraph(attributePaths = "author")
  Page<BoardPost> findByCategoryOrderByIdDesc(BoardCategory category, Pageable pageable);

  @EntityGraph(attributePaths = "author")
  @Query(value = """
      select p from BoardPost p
      where (:category is null or p.category = :category)
        and (:keyword = '' or lower(p.title) like lower(concat('%', :keyword, '%'))
          or p.content like concat('%', :keyword, '%'))
      order by case when p.category = com.example.ex76.entity.BoardCategory.NOTICE then 0 else 1 end,
        p.pinned desc, p.pinOrder asc, p.id desc
      """,
      countQuery = """
      select count(p) from BoardPost p
      where (:category is null or p.category = :category)
        and (:keyword = '' or lower(p.title) like lower(concat('%', :keyword, '%'))
          or p.content like concat('%', :keyword, '%'))
      """)
  Page<BoardPost> search(@Param("category") BoardCategory category,
                         @Param("keyword") String keyword, Pageable pageable);

  long countByAuthor_Email(String email);
  long countByAuthor_EmailAndCategory(String email, BoardCategory category);

  @Query("select coalesce(sum(p.likeCount), 0) from BoardPost p where p.author.email = :email")
  long sumLikeCountByAuthorEmail(@Param("email") String email);

  List<BoardPost> findByPinnedTrueOrderByPinOrderAscIdDesc();

  @Query("select coalesce(max(p.pinOrder), 0) from BoardPost p where p.pinned = true")
  int findMaxPinOrder();

  @EntityGraph(attributePaths = "author")
  List<BoardPost> findTop5ByAuthor_EmailOrderByIdDesc(String email);
}
