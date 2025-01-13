package com.mattordre.summitstore.product.controller;


import com.mattordre.summitstore.product.dto.CreateShoesDTO;
import com.mattordre.summitstore.product.service.ShoesService;
import com.mattordre.summitstore.product.model.Shoes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/product/shoes")
public class ShoesController {

    private final ShoesService shoesService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Shoes createShoes(@Valid @RequestBody CreateShoesDTO createShoesDTO) {
        return shoesService.createShoes(createShoesDTO);
        //return shoesService.getShoesById(id);
    }

}
