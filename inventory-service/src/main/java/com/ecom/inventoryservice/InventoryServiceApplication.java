package com.ecom.inventoryservice;

import com.ecom.inventoryservice.model.Inventory;
import com.ecom.inventoryservice.repository.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class InventoryServiceApplication {

	public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
	}

    @Bean
    public CommandLineRunner loadData(InventoryRepository inventoryRepository){
        return args -> {
            Inventory obj1 = new Inventory();
            obj1.setSkuCode("Iphone_13");
            obj1.setQuantity(100);

            Inventory obj2 = new Inventory();
            obj2.setSkuCode("Samsung Galaxy S25 ");
            obj2.setQuantity(100);

            inventoryRepository.save(obj1);
            inventoryRepository.save(obj2);
        };

    }

}
