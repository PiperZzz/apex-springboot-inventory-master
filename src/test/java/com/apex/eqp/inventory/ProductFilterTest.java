package com.apex.eqp.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.apex.eqp.inventory.entities.Product;
import com.apex.eqp.inventory.helpers.ProductFilter;

public class ProductFilterTest {
    @Test
    void testRemoveRecalledFrom() {

        Product apple = new Product(1, "apple", 1.50, 10);
        Product cookies = new Product(2, "cookies", 2.50, 10);
        Product gum = new Product(3, "gum", 3.50, 11);

        List<Product> allProducts = Arrays.asList(apple, cookies, gum);

        Set<String> recalledProducts = new HashSet<>(Arrays.asList("gum"));

        ProductFilter filter = new ProductFilter(recalledProducts);

        List<Product> filteredProducts = filter.removeRecalledFrom(allProducts);

        assertEquals(2, filteredProducts.size());
        assertTrue(filteredProducts.contains(apple));
        assertTrue(filteredProducts.contains(cookies));
        assertFalse(filteredProducts.contains(gum));
    }
}
