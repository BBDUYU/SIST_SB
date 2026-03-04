package org.doit.ik.complex;

import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/complex")
public class ComplexApiController {

    private final ComplexRepository complexRepository;

    public ComplexApiController(ComplexRepository complexRepository) {
        this.complexRepository = complexRepository;
    }

    @GetMapping("/in-bounds")
    public List<Complex> inBounds(
        @RequestParam("swLat") Double swLat,
        @RequestParam("swLng") Double swLng,
        @RequestParam("neLat") Double neLat,
        @RequestParam("neLng") Double neLng,
        @RequestParam(value="type", required=false) String type,         // APT/SH/OFFI
        @RequestParam(value="rentType", required=false) String rentType, // 전세/월세
        @RequestParam(value="areaMin", required=false) Double areaMin,   // ㎡ 기준
        @RequestParam(value="areaMax", required=false) Double areaMax    // ㎡ 기준
    ) {
        return complexRepository.findInBoundsWithFilters(
            swLat, swLng, neLat, neLng, type, rentType, areaMin, areaMax
        );
    }
    
    
}