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
        @RequestParam("neLng") Double neLng
    ) {
        return complexRepository.findByLatitudeBetweenAndLongitudeBetween(
            swLat, neLat, swLng, neLng
        );
    }
    
    
}