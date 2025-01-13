package com.mattordre.summitstore.product.controller;


import com.mattordre.summitstore.product.dto.CreateShoesDTO;
import com.mattordre.summitstore.product.model.Shoes;
import com.mattordre.summitstore.product.service.ShoesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/product/shoes")
public class ShoesController {

    private final ShoesService shoesService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Shoes createShoes(@Valid @RequestBody CreateShoesDTO createShoesDTO) {
        return shoesService.createShoes(createShoesDTO);
    }

}
