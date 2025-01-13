package com.mattordre.summitstore.brand.unit;

import com.mattordre.summitstore.brand.controller.BrandController;
import com.mattordre.summitstore.brand.dto.CreateBrandDTO;
import com.mattordre.summitstore.brand.model.Brand;
import com.mattordre.summitstore.brand.service.BrandService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class BrandControllerUnitTest {

    @InjectMocks
    private BrandController brandController;

    @Mock
    private BrandService brandService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void getBrands_shouldReturnListOfBrands() {
        // Generate testing data
        List<Brand> mockBrands = Arrays.asList(
                Brand.builder().id(1).name("Brand1").description("Description Brand1").build(),
                Brand.builder().id(2).name("Brand2").description("Description Brand2").build()
        );
        // Setup mocks
        when(brandService.getBrands()).thenReturn(mockBrands);
        // Execute method under test
        ResponseEntity<List<Brand>> response = brandController.getBrands();
        // Assert results
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockBrands, response.getBody());
        verify(brandService, times(1)).getBrands();
    }


    @Test
    void createBrand_shouldReturnCreatedBrand() {
        // Generate testing data
        CreateBrandDTO createBrandDTO =  CreateBrandDTO.builder()
                .name("new brand")
                .description("Description of new brand")
                .build();
        Brand savedBrand = Brand.builder()
                .id(1)
                .name("new brand")
                .description("Description of new brand")
                .build();
        // Setup mocks
        when(brandService.createBrand(createBrandDTO)).thenReturn(savedBrand);
        // Execute method under test
        ResponseEntity<Brand> response = brandController.createBrand(createBrandDTO);
        // Assert results
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(savedBrand, response.getBody());
        verify(brandService).createBrand(createBrandDTO);
    }


}
