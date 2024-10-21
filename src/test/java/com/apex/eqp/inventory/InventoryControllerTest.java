package com.apex.eqp.inventory;

import com.apex.eqp.inventory.controllers.InventoryController;
import com.apex.eqp.inventory.entities.Product;
import com.apex.eqp.inventory.services.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private InventoryController inventoryController;

    @Mock
    private ProductService productService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void before() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(inventoryController)
                .build();
    }

    @SneakyThrows
    @Test
    void shouldCallController() {
        mockMvc.perform(
                get("/api/inventory/product")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    void getAllProducts_ShouldReturnListOfProducts() throws Exception {
        Product product1 = new Product(1, "Product 1", 10.0, 5);
        Product product2 = new Product(2, "Product 2", 20.0, 10);
        when(productService.getAllProduct()).thenReturn(Arrays.asList(product1, product2));

        mockMvc.perform(get("/api/inventory/product"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Product 1"))
                .andExpect(jsonPath("$[1].name").value("Product 2"));
    }

    @Test
    void createProduct_ShouldReturnCreatedProduct() throws Exception {
        Product productToCreate = new Product(null, "New Product", 15.0, 7);
        Product createdProduct = new Product(3, "New Product", 15.0, 7);
        when(productService.save(any(Product.class))).thenReturn(createdProduct);

        mockMvc.perform(post("/api/inventory/product")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productToCreate)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("New Product"));
    }

    @Test
    void findProduct_WithExistingId_ShouldReturnProduct() throws Exception {
        Product product = new Product(1, "Product 1", 10.0, 5);
        when(productService.findById(1)).thenReturn(Optional.of(product));

        mockMvc.perform(get("/api/inventory/product/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Product 1"));
    }

    @Test
    void findProduct_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        when(productService.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/inventory/product/99"))
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    @Test
    void deleteProduct_shouldDeleteProduct() {
        Integer productId = 1;
        Product product = new Product();
        product.setId(productId);
        
        when(productService.findById(productId)).thenReturn(Optional.of(product));
        doNothing().when(productService).deleteById(productId);

        mockMvc.perform(
                delete("/api/inventory/product/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent());

        verify(productService, times(1)).findById(productId);
        verify(productService, times(1)).deleteById(productId);
    }

    @SneakyThrows
    @Test
    void deleteProduct_shouldReturnNotFoundWhenDeletingNonExistentProduct() {
        Integer productId = 999;
        
        when(productService.findById(productId)).thenReturn(Optional.empty());

        mockMvc.perform(
                delete("/api/inventory/product/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound());

        verify(productService, times(1)).findById(productId);
        verify(productService, never()).deleteById(anyInt());
    }

    @Test
    void updateProduct_shouldUpdateProduct() throws Exception {
        Integer productId = 1;
        Product updatedProduct = new Product(productId, "UpdatedProduct", 2.5, 15);
        
        when(productService.updateProduct(eq(productId), any(Product.class))).thenReturn(updatedProduct);

        mockMvc.perform(put("/api/inventory/product/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedProduct)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(updatedProduct)));
    }

    @Test
    void updateProduct_shouldReturn404WhenUpdatingNonExistentProduct() throws Exception {
        Integer nonExistentProductId = 999;
        Product updatedProduct = new Product(nonExistentProductId, "NonExistentProduct", 1.0, 1);
        
        when(productService.updateProduct(eq(nonExistentProductId), any(Product.class)))
                .thenThrow(new RuntimeException("Product not found"));

        mockMvc.perform(put("/api/inventory/product/{id}", nonExistentProductId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedProduct)))
                .andExpect(status().isNotFound());
    }
}
