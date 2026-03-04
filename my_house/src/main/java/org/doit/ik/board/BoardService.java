package org.doit.ik.board;

import java.util.List;

import org.doit.ik.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardService {

	private final BoardRepository boardRepository;
	private final SubCategoryRepository subCategoryRepository;
	private final ReplyRepository replyRepository;

	public Page<Board> list(String categoryCode, String q, Pageable pageable) {

		boolean hasQuery = (q != null && !q.isBlank());

		SubCategory sc = null;

		if (categoryCode != null && !categoryCode.equals("all")) {
			sc = subCategoryRepository.findByScName(toCategoryName(categoryCode))
					.orElseThrow(() -> new IllegalArgumentException("카테고리 없음: " + categoryCode));
		}

		if (hasQuery) {
			return boardRepository.search(BoardStatus.ACTIVE, sc, q, pageable);
		}

		if (sc == null) {
			return boardRepository.findByStatusOrderByCreatedAtDesc(BoardStatus.ACTIVE, pageable);
		}

		return boardRepository.findBySubCategoryAndStatusOrderByCreatedAtDesc(sc, BoardStatus.ACTIVE, pageable);
	}

	public Board get(Long id) {
		return boardRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("게시글 없음: " + id));
	}

	public Long create(BoardDTO dto, User writer) {

		SubCategory sc = subCategoryRepository.findByScName(toCategoryName(dto.getCategory()))
				.orElseThrow(() -> new IllegalArgumentException("카테고리 없음"));

		Board b = new Board();
		b.setTitle(dto.getTitle());
		b.setContent(dto.getContent());
		b.setUser(writer);
		b.setSubCategory(sc);
		// createdAt은 @PrePersist로 들어감
		return boardRepository.save(b).getBoardId();
	}

	public void update(Long id, BoardDTO dto, User me) {
	    Board b = get(id);

	    if (me == null || b.getUser() == null || !b.getUser().getUid().equals(me.getUid())) {
	        throw new IllegalStateException("작성자만 수정 가능");
	    }
	    
		b.setTitle(dto.getTitle());
		b.setContent(dto.getContent());

		SubCategory sc = subCategoryRepository.findByScName(toCategoryName(dto.getCategory()))
				.orElseThrow(() -> new IllegalArgumentException("카테고리 없음"));
		b.setSubCategory(sc);

		boardRepository.save(b);
	}

	private String toCategoryName(String code) {
		return switch (code) {
		case "notice" -> "공지사항";
		case "qna" -> "회원질문";
		default -> "자유게시판";
		};
	}
	
	// 리플
	public List<Reply> getReplies(Long boardId) {
	    Board board = get(boardId);
	    return replyRepository.findByBoardOrderByCreatedAtAsc(board);
	}

	public void addReply(Long boardId, String content, User writer) {
	    if (content == null || content.isBlank()) return;

	    Board board = get(boardId);

	    Reply r = new Reply();
	    r.setBoard(board);
	    r.setUser(writer);
	    r.setContent(content.trim());

	    replyRepository.save(r);
	}
	
	public void deleteReply(Long boardId, Long replyId, User me) {
	    if (me == null) throw new IllegalStateException("로그인 필요");

	    Reply r = replyRepository.findById(replyId)
	        .orElseThrow(() -> new IllegalArgumentException("댓글 없음"));

	    if (r.getBoard() == null || !r.getBoard().getBoardId().equals(boardId)) {
	        throw new IllegalStateException("잘못된 요청");
	    }

	    // 본인만 삭제
	    if (r.getUser() == null || !r.getUser().getUid().equals(me.getUid())) {
	        throw new IllegalStateException("본인만 삭제 가능");
	    }

	    replyRepository.delete(r);
	}
	
	public void delete(Long id, User me) {
	    Board b = get(id);

	    if (me == null || b.getUser() == null || !b.getUser().getUid().equals(me.getUid())) {
	        throw new IllegalStateException("작성자만 삭제 가능");
	    }

	    b.setStatus(BoardStatus.INACTIVE); // 소프트 삭제
	    boardRepository.save(b);
	}

}