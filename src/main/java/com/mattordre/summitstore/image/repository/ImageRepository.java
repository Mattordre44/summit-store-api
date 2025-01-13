package com.mattordre.summitstore.image.repository;

import com.mattordre.summitstore.image.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ImageRepository extends JpaRepository<Image, String> {}