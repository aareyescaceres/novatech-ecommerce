package com.novatech.store.catalog;

import com.novatech.store.catalog.CatalogService.ProductView;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class CatalogController {
    private final CatalogService catalog;
    public CatalogController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    public Page<ProductView> list(@RequestParam(required = false) String search,@RequestParam(required = false) String category,@RequestParam(required = false) String specKey,@RequestParam(required = false) String specValue,@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "12") int size) {
        return catalog.list(search, category, specKey, specValue, page, size);
    }

    @GetMapping("/{id}")
    public ProductView get(@PathVariable Long id) {
        return catalog.find(id);
    }
}
