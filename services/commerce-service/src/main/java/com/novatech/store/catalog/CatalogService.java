package com.novatech.store.catalog;

import com.novatech.store.shared.ApiException;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogService {
    public record ProductView(Long id, String sku, String name, String brand, String category, BigDecimal price, int stock, boolean active, String description, Map<String, String> specs, boolean detailsAvailable) {
    }

    public record ProductInput(@NotBlank@Size(max = 80) String sku,@NotBlank@Size(max = 200) String name,@NotBlank@Size(max = 100) String brand,@NotBlank@Size(max = 60) String category,@NotNull@DecimalMin("0.00")@Digits(integer = 10, fraction = 2) BigDecimal price,@Min(0) int stock,@NotNull Boolean active, String description, Map<String, String> specs) {
    }

    public record StockInput(@Min(0) int stock) {
    }

    private static final Logger log = LoggerFactory.getLogger(CatalogService.class);
    private final ProductRepository products;
    private final ProductDetailsRepository details;
    private final MongoTemplate mongo;
    public CatalogService(ProductRepository products, ProductDetailsRepository details, MongoTemplate mongo) {
        this.products = products;
        this.details = details;
        this.mongo = mongo;
    }

    @Transactional(readOnly = true)
    public Page<ProductView> list(String search, String category, String specKey, String specValue, int page, int size) {
        if (page< 0 || size< 1 || size> 50)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Paginacion incorrecta");
        Set<Long> filtered = null;
        if (specKey != null &&!specKey.isBlank()) {
            if (!specKey.matches("[A-Za-z0-9_-]{1,30}") || specValue == null || specValue.isBlank() || specValue.length()> 60)
                throw new ApiException(HttpStatus.BAD_REQUEST, "Filtro de especificacion invalido");
            try {
                Query q = new Query(Criteria.where("specs." + specKey).regex(Pattern.quote(specValue), "i"));
                filtered = mongo.find(q, ProductDetails.class).stream().map(d -> d.productId).collect(Collectors.toSet());
            }
            catch (Exception ex) {
                throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Filtro NoSQL no disponible");
            }
        }
        Set<Long> ids = filtered;
        Specification<Product> spec =(root, query, cb) -> {
            List<Predicate> conditions = new ArrayList<>();
            conditions.add(cb.isTrue(root.get("active")));
            if (search != null &&!search.isBlank()) {
                String term = "%" + search.toLowerCase(Locale.ROOT).replace("%", "\\%").replace("_", "\\_") + "%";
                conditions.add(cb.or(cb.like(cb.lower(root.get("name")), term, '\\'), cb.like(cb.lower(root.get("brand")), term, '\\'), cb.like(cb.lower(root.get("sku")), term, '\\')));
            }
            if (category != null &&!category.isBlank()) conditions.add(cb.equal(root.get("category"), category));
            if (ids != null) conditions.add(root.get("id").in(ids));
            return cb.and(conditions.toArray(new Predicate[0]));
        }
        ;
        Page<Product> result = products.findAll(spec, PageRequest.of(page, size, Sort.by("id").descending()));
        Map<Long, ProductDetails> docs = loadDetails(result.getContent());
        return result.map(p -> toView(p, docs.get(p.id)));
    }

    @Transactional(readOnly = true)
    public ProductView find(Long id) {
        Product p = products.findById(id).filter(x -> x.active).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Producto inexistente"));
        return toView(p, loadDetails(List.of(p)).get(p.id));
    }

    @Transactional(readOnly = true)
    public List<ProductView> allAdmin() {
        List<Product> list = products.findAll(Sort.by("id").descending());
        Map<Long, ProductDetails> docs = loadDetails(list);
        return list.stream().map(p -> toView(p, docs.get(p.id))).toList();
    }

    @Transactional
    public ProductView create(ProductInput in) {
        validateDetails(in);
        if (products.existsBySku(in.sku()))
            throw new ApiException(HttpStatus.CONFLICT, "SKU duplicado");
        Product p = new Product();
        updateFields(p, in);
        p = products.saveAndFlush(p);
        saveDetails(p.id, in);
        return toView(p, loadDetails(List.of(p)).get(p.id));
    }

    @Transactional
    public ProductView update(Long id, ProductInput in) {
        validateDetails(in);
        Product p = products.lockById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Producto inexistente"));
        if (!p.sku.equals(in.sku()) && products.existsBySku(in.sku()))
            throw new ApiException(HttpStatus.CONFLICT, "SKU duplicado");
        // Stock se modifica por el endpoint especifico para evitar cambios accidentales.
        p.sku = in.sku().strip();
        p.name = in.name().strip();
        p.brand = in.brand().strip();
        p.category = in.category().strip();
        p.price = in.price();
        p.active = in.active();
        saveDetails(id, in);
        return toView(p, loadDetails(List.of(p)).get(p.id));
    }

    @Transactional
    public ProductView setStock(Long id, int stock) {
        Product p = products.lockById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Producto inexistente"));
        p.stock = stock;
        return toView(p, loadDetails(List.of(p)).get(p.id));
    }

    private void updateFields(Product p, ProductInput in) {
        p.sku = in.sku().strip();
        p.name = in.name().strip();
        p.brand = in.brand().strip();
        p.category = in.category().strip();
        p.price = in.price();
        p.stock = in.stock();
        p.active = in.active();
    }

    private void validateDetails(ProductInput in) {
        Map<String, String> specs = in.specs() == null ? Map.of(): in.specs();
        if (specs.size()>25 || specs.entrySet().stream().anyMatch(e ->!e.getKey().matches("[A-Za-z0-9_-]{1,30}") || e.getValue() == null || e.getValue().length()>200))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Especificaciones invalidas");
        if (in.description() != null && in.description().length()>3000)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Descripcion demasiado larga");
    }

    private void saveDetails(Long id, ProductInput in) {
        Map<String, String> specs = in.specs() == null ? Map.of(): in.specs();
        try {
            details.save(new ProductDetails(id, in.description() == null ? "": in.description(), specs));
        }
        catch (Exception ex) {
            log.warn("MongoDB no disponible al guardar ficha de producto {}: {}", id, ex.getMessage());
            // La ficha comercial SQL queda disponible; un administrador puede reintentar los detalles.
        }
    }

    private Map<Long, ProductDetails> loadDetails(List<Product> list) {
        if (list.isEmpty())
            return Map.of();
        try {
            List<Long> ids = list.stream().map(p -> p.id).toList();
            Map<Long, ProductDetails> byId = new HashMap<>();
            details.findAllById(ids).forEach(d -> byId.put(d.productId, d));
            return byId;
        }
        catch (Exception ex) {
            log.warn("MongoDB no disponible; se muestran datos SQL: {}", ex.getMessage());
            return Map.of();
        }
    }

    private ProductView toView(Product p, ProductDetails d) {
        return new ProductView(p.id, p.sku, p.name, p.brand, p.category, p.price, p.stock, p.active, d == null ? "": d.description, d == null ? Map.of(): d.specs, d != null);
    }
}
