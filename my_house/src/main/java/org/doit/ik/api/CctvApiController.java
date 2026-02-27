package org.doit.ik.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cctv")
@RequiredArgsConstructor
public class CctvApiController {
    private final CctvRepository repository;

    @GetMapping
    public List<Cctv> getCctvs(
        @RequestParam("minLat") Double minLat, 
        @RequestParam("maxLat") Double maxLat, 
        @RequestParam("minLng") Double minLng, 
        @RequestParam("maxLng") Double maxLng
    ) {
        if (minLat == null) return repository.findAll();
        return repository.findByLocationRange(minLat, maxLat, minLng, maxLng);
    }
}
