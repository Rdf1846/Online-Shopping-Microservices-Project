package com.microserviceProject.InventoryService.controller;

import com.microserviceProject.InventoryService.dto.InventoryResponseDto;
import com.microserviceProject.InventoryService.service.InventoryService;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponseDto> inStockOrNot(@RequestParam List<String> skuCode)
    {
        return inventoryService.inStockOrNot(skuCode);

    }

}
