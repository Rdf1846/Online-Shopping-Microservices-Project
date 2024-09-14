package com.microserviceProject.InventoryService.service;

import com.microserviceProject.InventoryService.dto.InventoryResponseDto;
import com.microserviceProject.InventoryService.model.Inventory;
import com.microserviceProject.InventoryService.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
//    @SneakyThrows
    public List<InventoryResponseDto> inStockOrNot(List<String> skuCode) {
//        logger.info("wait started");
////        Thread.sleep(10000);
//        logger.info("wait ended");


        logger.info("Sku codes in inventory service are: {}"+skuCode);

        List<Inventory> inventories = inventoryRepository.findBySkuCodeIn(skuCode);
        if (inventories.isEmpty()) {
            logger.warn("No inventory found for the given SKU codes :{}",skuCode);
            return skuCode.stream()
                    .map(sku -> InventoryResponseDto.builder()
                            .skuCode(sku)
                            .inStockOrNot(false) // Mark as out of stock if not found
                            .build())
                    .collect(toList());  // Return empty response if no inventories found
        }

        List<InventoryResponseDto> response = inventories
               .stream()
               .map(inventory -> InventoryResponseDto.builder()
                       .skuCode(inventory.getSkuCode())
                       .inStockOrNot(inventory.getQuantity() > 0)
                       .build()
    ).collect(Collectors.toList());
        logger.info("InventoryService Response: {}", response);
        return response;

    }

}
