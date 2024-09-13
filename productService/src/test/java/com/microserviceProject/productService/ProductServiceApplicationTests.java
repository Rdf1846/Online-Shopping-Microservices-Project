package com.microserviceProject.productService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microserviceProject.productService.dto.ProductRequestDto;
import com.microserviceProject.productService.dto.ProductResponseDto;
import com.microserviceProject.productService.model.Product;
import com.microserviceProject.productService.repository.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.http.MediaType;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc

class ProductServiceApplicationTests {

	@Container
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");

	@Autowired
	private  MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProductRepository productRepository;

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry)
	{
		dynamicPropertyRegistry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);

	}

	@Test
	void createProductTest() throws Exception {
		//setup test products data
		ProductRequestDto productRequestDto = getProductRequestDto();

		//since mockMvc content() method accepts STRING datatype, so converting DTO to String using objectMapper class
		String productRequestString = objectMapper.writeValueAsString(productRequestDto);

		//Performing POST request to create a product
		mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
				.contentType(APPLICATION_JSON)
				.content(productRequestString))
				.andExpect(MockMvcResultMatchers.status().isCreated());

		//validate that the product is created
		Assertions.assertEquals(1,productRepository.findAll().size());

	}

	private ProductRequestDto getProductRequestDto() {
		//setup a dummmy data into prodcutDto using @Builder annotation - alternative to setter method
		return ProductRequestDto.builder()
				.name("led")
				.description("Samsung 32 inch led")
				.price(BigDecimal.valueOf(32000))
				.build();
	}


	@Test
	void getAllProductsTest() throws Exception {
		//ensuring we have products in the repository
		ProductRequestDto productRequestDto1 = getProductRequestDto();
		ProductRequestDto productRequestDto2 = getProductRequestDto1();

		//mapping or converting product dto to entity - bcoz repository.save() method accepts ENTITY as argument
		Product product1 = Product.builder()
				.name(productRequestDto1.getName())
				.description(productRequestDto1.getDescription())
				.price(productRequestDto1.getPrice())
				.build();

		Product product2 = Product.builder()
				.name(productRequestDto2.getName())
				.description(productRequestDto2.getDescription())
				.price(productRequestDto2.getPrice())
				.build();

		//saving the product entity into the repository
		productRepository.save(product1);
		productRepository.save(product2);

		//performing GET request to retrieve all products
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/product")
				.contentType(APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andReturn();

		//converting JSON response to a list of products
		String jsonResponse = mvcResult.getResponse().getContentAsString();
		List<ProductResponseDto> productResponseDtoList = objectMapper.readValue(jsonResponse,
				new TypeReference<List<ProductResponseDto>>() {});

		//validates the response contains the expected no of products
		Assertions.assertEquals(3, productResponseDtoList.size());


	}

	private ProductRequestDto getProductRequestDto1() {
		return ProductRequestDto.builder()
				.name("mobile")
				.description("Samsung s24 ultra pro max")
				.price(BigDecimal.valueOf(90000))
				.build();
	}
}
