package org.doit.ik.board;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardRepository extends JpaRepository<Board, Long>{

	// 목록 조회
	Page<Board> findByStatusOrderByCreatedAtDesc(BoardStatus status, Pageable pageable);

	// 카테고리별 조회
	Page<Board> findBySubCategoryAndStatusOrderByCreatedAtDesc(
			SubCategory subCategory,
			BoardStatus status,
			Pageable pageable
			);

	// 검색
	@Query("""
			    SELECT b FROM Board b
			    WHERE b.status = :status
			      AND (:sc IS NULL OR b.subCategory = :sc)
			      AND (
			            b.title LIKE %:q%
			         OR b.content LIKE %:q%
			      )
			    ORDER BY b.createdAt DESC
			""")
	Page<Board> search(
			@Param("status") BoardStatus status,
			@Param("sc") SubCategory sc,
			@Param("q") String q,
			Pageable pageable
			);
}