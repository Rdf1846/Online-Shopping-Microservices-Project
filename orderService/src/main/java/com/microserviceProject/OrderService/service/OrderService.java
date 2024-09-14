package com.microserviceProject.OrderService.service;

import com.microserviceProject.OrderService.dto.InventoryResponse;
import com.microserviceProject.OrderService.dto.OrderLineItemsDto;
import com.microserviceProject.OrderService.dto.OrderRequestDto;
import com.microserviceProject.OrderService.event.OrderPlacedEvent;
import com.microserviceProject.OrderService.model.Order;
import com.microserviceProject.OrderService.model.OrderLineItems;
import com.microserviceProject.OrderService.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);


    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;


    public String placeOrder(OrderRequestDto orderRequestDto)
    {
        //CONVERTING orderRequestDto to Order Entity
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

         List<OrderLineItems> orderLineItemsList = orderRequestDto.getOrderLineItemsDtoList().stream()
                .map(this::mapOrderLineItemsDto_To_OrderLineItemsEntity)
                .toList();

         order.setOrderLineItemsList(orderLineItemsList);

        List<String> skuCodesList = order.getOrderLineItemsList().stream().
                map(OrderLineItems::getSkuCode)
                .toList();
        System.out.println("SKU Codes List: " + skuCodesList);


        //calling Inventory service to check whether the product requested by user is in stock or not. Accordingly, we will place the order
       InventoryResponse[] inventoryResponseDtosArray =  webClientBuilder.build().get()
                .uri("http://inventoryService/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode",String.join(",", skuCodesList)).build())
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .retrieve()
                                .bodyToMono(InventoryResponse[].class)
               .doOnNext(response -> logger.info("Raw Inventory Response: {}", Arrays.toString(response)))
               .block();

        logger.info("Inventory Response: {}", Arrays.toString(inventoryResponseDtosArray));

        // Check if inventoryResponseDtosArray is null or empty
        if (inventoryResponseDtosArray == null || inventoryResponseDtosArray.length == 0) {
            return "Error: Unable to fetch inventory details. Please try again later.";
        }

        List<String> unavailableProducts = new ArrayList<>();
        List<String> availableProducts = new ArrayList<>();

        for (int i = 0; i < inventoryResponseDtosArray.length; i++) {
            if (inventoryResponseDtosArray[i].isInStockOrNot()) {
                availableProducts.add(skuCodesList.get(i));
            } else {
                unavailableProducts.add(skuCodesList.get(i));
            }
        }

        if (unavailableProducts.isEmpty()) {
            // All products are in stock, proceed with the order
            orderRepository.save(order);
            logger.info("Product available= true, so saving the product in order table");

            logger.info("order placed, sending the kafka topic to the consumer - which is notificationService to send the message to customer");
            //Send the message to kafka - order number
            kafkaTemplate.send("notificationTopic", new OrderPlacedEvent(order.getOrderNumber()));

            logger.info("kafka topic successfully sent!");

            return "Order placed successfully. All products are in stock.";
        } else {
            // Some products are not in stock, provide feedback
            String unavailableMessage = String.join(", ", unavailableProducts);
            String availableMessage = String.join(", ", availableProducts);

            if(availableMessage.isEmpty())
            {
                return "The following products are not in stock: " + unavailableMessage;
            }
            else {

                return "The following products are not in stock: " + unavailableMessage +
                        ". The products available in our stock are: " + availableMessage;
            }
        }

    }

    private OrderLineItems mapOrderLineItemsDto_To_OrderLineItemsEntity(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        return orderLineItems;
    }

}
