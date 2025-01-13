package com.mattordre.summitstore.product.service;

import com.mattordre.summitstore.product.model.Product;
import com.mattordre.summitstore.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ProductService {


    private final ProductRepository productRepository;

    public List<Product> getAllProduct() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductByName(String productName) {
        return productRepository.findByName(productName);
    }

}
