package com.mattordre.summitstore.product.repository;

import com.mattordre.summitstore.product.model.Shoes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ShoesRepository extends JpaRepository<Shoes, UUID> {}