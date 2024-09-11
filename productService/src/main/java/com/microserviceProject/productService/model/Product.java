package com.microserviceProject.productService.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
@Builder
@Document(value = "product")
public class Product {

    @Id
    private String id;

    private String name;
    private String description;
    private BigDecimal price;

}
