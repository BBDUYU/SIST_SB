package org.doit.ik.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class LhNoticeApiController {
	
	private final LhNoticeRepository repository;
	
	@GetMapping
    public List<LhNotice> getAllNotices() {
        return repository.findAll().stream()
                .filter(n -> "공고중".equals(n.getPanSs()))
                .toList();
    }
}
