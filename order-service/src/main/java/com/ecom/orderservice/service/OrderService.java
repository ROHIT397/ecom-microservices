package com.ecom.orderservice.service;

import com.ecom.orderservice.dto.InventoryResponse;
import com.ecom.orderservice.dto.OrderLineItemsDto;
import com.ecom.orderservice.dto.OrderRequest;
import com.ecom.orderservice.model.Order;
import com.ecom.orderservice.model.OrderLineItems;
import com.ecom.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder; // Use Builder for Load Balancing

    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();
        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes = order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getSkuCode).toList();

        log.info("Checking inventory for SKUs: {}", skuCodes);

        // 1. External Network Call (Performed OUTSIDE the DB Transaction)
        InventoryResponse[] inventoryResponses = webClientBuilder.build().get()
                .uri("http://inventory-service/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        if (inventoryResponses == null || inventoryResponses.length != skuCodes.size()) {
            log.error("Inventory check failed. Expected {} results, got {}", skuCodes.size(),
                    inventoryResponses == null ? 0 : inventoryResponses.length);
            throw new IllegalArgumentException("Inventory check failed: Item mismatch.");
        }

        boolean allProductsInStock = Arrays.stream(inventoryResponses)
                .allMatch(res -> Boolean.TRUE.equals(res.getIsInStock()));

        if (allProductsInStock) {
            saveOrder(order); // Call internal transactional method
            log.info("Order {} placed successfully", order.getOrderNumber());
        } else {
            throw new IllegalArgumentException("Product out of stock. Order cancelled.");
        }
    }

    // 2. Database Operation (Performed INSIDE the Transaction)
    @Transactional
    protected void saveOrder(Order order) {
        orderRepository.save(order);
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}