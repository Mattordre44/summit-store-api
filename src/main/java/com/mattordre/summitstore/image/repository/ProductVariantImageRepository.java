package com.mattordre.summitstore.image.repository;

import com.mattordre.summitstore.image.model.ProductVariantImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductVariantImageRepository extends JpaRepository<ProductVariantImage, String> {}
