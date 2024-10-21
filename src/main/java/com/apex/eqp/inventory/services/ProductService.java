package com.apex.eqp.inventory.services;

import com.apex.eqp.inventory.entities.Product;
import com.apex.eqp.inventory.entities.RecalledProduct;
import com.apex.eqp.inventory.helpers.ProductFilter;
import com.apex.eqp.inventory.repositories.InventoryRepository;
import com.apex.eqp.inventory.repositories.RecalledProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final InventoryRepository inventoryRepository;
    private final RecalledProductRepository recalledProductRepository;

    @Transactional
    public Product save(Product product) {
        return inventoryRepository.save(product);
    }

    public Collection<Product> getAllProducts() {
        Set<String> recalledProductNames = recalledProductRepository.findAll()
                .stream()
                .map(RecalledProduct::getName)
                .collect(Collectors.toSet());

        ProductFilter filter = new ProductFilter(recalledProductNames);

        return filter.removeRecalledFrom(inventoryRepository.findAll());
    }

    public Optional<Product> findById(Integer id) {
        return inventoryRepository.findById(id);
    }

    @Transactional
    public void deleteById(Integer id) {
        inventoryRepository.deleteById(id);
    }

    @Transactional
    public Product updateProduct(Integer id, Product updatedProduct) {
        return inventoryRepository.findById(id)
            .map(existingProduct -> {
                existingProduct.setName(updatedProduct.getName());
                existingProduct.setPrice(updatedProduct.getPrice());
                existingProduct.setQuantity(updatedProduct.getQuantity());
                return inventoryRepository.save(existingProduct);
            })
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }
}
