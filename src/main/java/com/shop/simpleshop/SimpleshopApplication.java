package com.shop.simpleshop;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Simple Shop Inventory API", version = "1.0", description = "Backend API for managing products and inventory in a simple shop application"))
public class SimpleshopApplication {

	public static void main(String[] args) {
		SpringApplication.run(SimpleshopApplication.class, args);
	}

}
