package com.mattordre.summitstore.brand.unit;

import com.mattordre.summitstore.brand.repository.BrandRepository;
import com.mattordre.summitstore.brand.service.BrandService;
import com.mattordre.summitstore.brand.dto.CreateBrandDTO;
import com.mattordre.summitstore.brand.model.Brand;
import com.mattordre.summitstore.exception.InvalidArgumentException;
import com.mattordre.summitstore.image.model.BrandLogo;
import com.mattordre.summitstore.image.model.ImageType;
import com.mattordre.summitstore.image.repository.BrandLogoRepository;
import com.mattordre.summitstore.image.service.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BrandServiceUnitTest {

    @InjectMocks
    private BrandService brandService;

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private BrandLogoRepository brandLogoRepository;

    @Mock
    private ImageService imageService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void getBrands_shouldReturnListOfBrands() {
        // Prepare testing data
        BrandLogo brandLogo1 = BrandLogo.builder().fileName("brand1.png").bucketName("bucketName").type(ImageType.BRAND).build();
        BrandLogo brandLogo2 = BrandLogo.builder().fileName("brand2.png").bucketName("bucketName").type(ImageType.BRAND).build();
        List<Brand> expectedBrands = Arrays.asList(
                Brand.builder().id(1).name("Brand1").description("Description Brand1").logo(brandLogo1).build(),
                Brand.builder().id(2).name("Brand2").description("Description Brand2").logo(brandLogo2).build()
        );

        // Setup mocks
        when(brandRepository.findAll()).thenReturn(expectedBrands);

        // Execute method under test
        List<Brand> actualBrands = brandService.getBrands();

        // Assert results
        assertEquals(expectedBrands.size(), actualBrands.size());
        assertEquals(expectedBrands, actualBrands);
        verify(brandRepository, times(1)).findAll();
    }


    @Test
    void getBrandById_shouldReturnBrand_whenBrandExists() {
        // Prepare testing data
        Integer brandId = 1;
        Brand expectedBrand = Brand.builder().id(brandId).name("Brand1").description("Description Brand1").build();

        // Setup mocks
        when(brandRepository.findById(brandId)).thenReturn(Optional.of(expectedBrand));

        // Execute method under test
        Brand actualBrand = brandService.getBrandById(brandId);

        // Assert results
        assertNotNull(actualBrand);
        assertEquals(expectedBrand, actualBrand);
        verify(brandRepository, times(1)).findById(brandId);
    }


    @Test
    void getBrandById_shouldThrowException_whenBrandDoesNotExist() {
        // Prepare testing data
        Integer brandId = 1;

        // Setup mocks
        when(brandRepository.findById(brandId)).thenReturn(Optional.empty());

        // Execute method under test & Assert results
        assertThrows(NoSuchElementException.class, () -> brandService.getBrandById(brandId));
        verify(brandRepository, times(1)).findById(brandId);
    }


    @Test
    void createBrand_shouldReturnCreatedBrand() {
        // Prepare testing data
        CreateBrandDTO createBrandDTO = CreateBrandDTO.builder()
                .name("Brand1")
                .description("Description Brand1")
                .imageFileName("brand1.png")
                .build();
        BrandLogo brandLogo = BrandLogo.builder().fileName("brand1.png").bucketName("bucketName").type(ImageType.BRAND).build();
        Brand savedBrand = Brand.builder().id(1).name("Brand1").description("Description Brand1").logo(brandLogo).build();

        // Setup mocks
        when(brandLogoRepository.save(any(BrandLogo.class))).thenReturn(brandLogo);
        when(brandRepository.save(any(Brand.class))).thenReturn(savedBrand);
        when(imageService.isImageFileUploaded(anyString(), any())).thenReturn(true);

        // Execute method under test
        Brand actualBrand = brandService.createBrand(createBrandDTO);

        // Assert results
        assertNotNull(actualBrand);
        assertEquals(savedBrand, actualBrand);
        verify(brandLogoRepository, times(1)).save(any(BrandLogo.class));
        verify(brandRepository, times(1)).save(any(Brand.class));
    }


    @Test
    void createBrand_shouldThrowInvalidArgumentException_whenImageIsNotUploaded() {
        // Prepare testing data
        CreateBrandDTO createBrandDTO = CreateBrandDTO.builder()
                .name("Brand1")
                .description("Description Brand1")
                .imageFileName("brand1.png")
                .build();

        // Setup mocks
        when(imageService.isImageFileUploaded(anyString(), any())).thenReturn(false);

        // Execute method under test & Assert results
        assertThrows(InvalidArgumentException.class, () -> brandService.createBrand(createBrandDTO));
        verify(brandRepository, times(0)).save(any(Brand.class));
        verify(brandLogoRepository, times(0)).save(any(BrandLogo.class));
    }

}
