package com.microserviceProject.productService.service;

import com.microserviceProject.productService.dto.ProductRequestDto;
import com.microserviceProject.productService.dto.ProductResponseDto;
import com.microserviceProject.productService.model.Product;
import com.microserviceProject.productService.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public void createProduct(ProductRequestDto productRequestDto)
    {
        Product product = Product.builder()
                .name(productRequestDto.getName())
                .description(productRequestDto.getDescription())
                .price(productRequestDto.getPrice())
                .build();

        productRepository.save(product);
        log.info("Product {} save successfully", product.getId());

    }

    public List<ProductResponseDto> getAllProducts()
    {
        List<Product> allProduct = productRepository.findAll();
       List<ProductResponseDto> allProductList = allProduct.stream().
               map(this::mapProductEntityToProductResponseDto).toList();

        return allProductList;
    }

    private ProductResponseDto mapProductEntityToProductResponseDto(Product product) {
        return ProductResponseDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();

    }

}
