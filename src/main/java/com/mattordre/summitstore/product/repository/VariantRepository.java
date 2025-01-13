package com.mattordre.summitstore.product.repository;

import com.mattordre.summitstore.product.model.Variant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VariantRepository extends JpaRepository<Variant, UUID> {
}
