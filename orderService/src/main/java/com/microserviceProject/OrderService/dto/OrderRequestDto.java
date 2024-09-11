package com.microserviceProject.OrderService.dto;

import com.microserviceProject.OrderService.model.OrderLineItems;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDto {

    private List<OrderLineItemsDto> orderLineItemsDtoList;
}
