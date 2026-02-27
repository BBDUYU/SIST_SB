package org.doit.ik.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/safe-paths")
@RequiredArgsConstructor
public class SafePathApiController {

    private final SafeReturnPathRepository repository;

    @GetMapping
    public List<SafeReturnPath> getAllPaths() {
        return repository.findAll();
    }
}