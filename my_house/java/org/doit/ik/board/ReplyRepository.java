package org.doit.ik.board;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

	// 게시글 기준 댓글 목록
	List<Reply> findByBoardOrderByCreatedAtAsc(Board board);


}