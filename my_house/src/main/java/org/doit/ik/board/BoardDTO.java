package org.doit.ik.board;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardDTO {
    private String category; // notice/free/qna
    private String title;
    private String content;
}