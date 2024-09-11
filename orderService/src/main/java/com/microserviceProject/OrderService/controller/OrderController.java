package com.microserviceProject.OrderService.controller;

import com.microserviceProject.OrderService.dto.OrderRequestDto;
import com.microserviceProject.OrderService.model.Order;
import com.microserviceProject.OrderService.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @CircuitBreaker(name="inventory", fallbackMethod = "fallbackMethod")
    @TimeLimiter(name="inventory")
    @Retry(name="inventory")
    public CompletableFuture<ResponseEntity<String>> placeOrder(@RequestBody OrderRequestDto orderRequestDto)
    {
        String result = orderService.placeOrder(orderRequestDto);

        if(result.contains("Order placed successfully"))
        {
            // Return a 201 Created status if the order is placed successfully
            return CompletableFuture.supplyAsync(()->new ResponseEntity<>(result, HttpStatus.CREATED));
        }
        else {
            // Returns a 400 Bad Request status if any products are unavailable with product details
            return CompletableFuture.supplyAsync(()->new ResponseEntity<>(result, HttpStatus.BAD_REQUEST));
        }
    }

    public CompletableFuture<ResponseEntity<String>> fallbackMethod(OrderRequestDto orderRequestDto, RuntimeException runtimeException)
    {
        String result = "Something went wrong with inventory, pls try after sometime";
        return CompletableFuture.supplyAsync(()->new ResponseEntity<>(result, HttpStatus.OK));
    }


}
