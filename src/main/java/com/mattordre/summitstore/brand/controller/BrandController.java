package com.mattordre.summitstore.brand.controller;

import com.mattordre.summitstore.brand.dto.CreateBrandDTO;
import com.mattordre.summitstore.brand.model.Brand;
import com.mattordre.summitstore.brand.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/brand")
public class BrandController {

    private final BrandService brandService;


    @GetMapping
    public ResponseEntity<List<Brand>> getBrands() {
        List<Brand> brand =  brandService.getBrands();
        return ResponseEntity.ok(brand);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Brand> getBrandById(@PathVariable Integer id) {
        Brand brand = brandService.getBrandById(id);
        return ResponseEntity.ok(brand);
    }


    @PostMapping
    public ResponseEntity<Brand> createBrand(@Valid @RequestBody CreateBrandDTO createBrandDTO) {
        Brand brand =  brandService.createBrand(createBrandDTO);
        return ResponseEntity.ok(brand);
    }

}
