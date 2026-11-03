package com.example.ProductManagementSystem;

import com.example.ProductManagementSystem.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.net.URI;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class ProductManagementSystemApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ProductRepository productRepository;

	@BeforeEach
	void clearProducts() {
		productRepository.deleteAll();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void productCanBeCreatedListedUpdatedAndDeleted() throws Exception {
		MvcResult createResult = mockMvc.perform(post("/api/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "productName": "Desk lamp",
								  "category": "Lighting",
								  "price": 24.99,
								  "quantity": 12,
								  "description": "LED desk lamp"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.productName").value("Desk lamp"))
				.andReturn();
		String location = createResult.getResponse().getHeader("Location");
		String path = URI.create(location).getPath();
		long productId = Long.parseLong(path.substring(path.lastIndexOf('/') + 1));

		mockMvc.perform(get("/api/products").param("category", "lighting"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(productId));

		mockMvc.perform(put("/api/products/{id}", productId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "productName": "Desk lamp pro",
								  "category": "Lighting",
								  "price": 34.99,
								  "quantity": 8,
								  "description": "Updated LED desk lamp"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.productName").value("Desk lamp pro"));

		mockMvc.perform(delete("/api/products/{id}", productId))
				.andExpect(status().isNoContent());
		mockMvc.perform(get("/api/products/{id}", productId))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message", containsString("was not found")));
	}

	@Test
	void invalidProductReturnsValidationDetails() throws Exception {
		mockMvc.perform(post("/api/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "productName": "",
								  "category": "",
								  "price": 0,
								  "quantity": -1
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.validationErrors.productName").exists())
				.andExpect(jsonPath("$.validationErrors.price").exists())
				.andExpect(jsonPath("$.validationErrors.quantity").exists());
	}
}
