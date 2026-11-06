package com.novatech.store.catalog;

import com.novatech.store.cart.Cart;
import com.novatech.store.cart.CartRepository;
import com.novatech.store.user.AppUser;
import com.novatech.store.user.UserRepository;
import java.math.BigDecimal;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DemoDataInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);
    private final UserRepository users;
    private final CartRepository carts;
    private final ProductRepository products;
    private final ProductDetailsRepository details;
    private final PasswordEncoder encoder;
    @Value("${app.admin.email}")
    String adminEmail;
    @Value("${app.admin.password}")
    String adminPassword;
    @Value("${app.staff.sales-email}")
    String salesEmail;
    @Value("${app.staff.sales-password}")
    String salesPassword;
    @Value("${app.staff.inventory-email}")
    String inventoryEmail;
    @Value("${app.staff.inventory-password}")
    String inventoryPassword;
    @Value("${app.seed}")
    boolean seed;
    public DemoDataInitializer(UserRepository users, CartRepository carts, ProductRepository products, ProductDetailsRepository details, PasswordEncoder encoder) {
        this.users = users;
        this.carts = carts;
        this.products = products;
        this.details = details;
        this.encoder = encoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!seed) return;
        if (!users.existsByEmail(adminEmail.toLowerCase())) {
            AppUser admin = new AppUser();
            admin.email = adminEmail.toLowerCase();
            admin.passwordHash = encoder.encode(adminPassword);
            admin.role = "ADMIN";
            admin = users.save(admin);
            Cart cart = new Cart();
            cart.user = admin;
            carts.save(cart);
            log.warn("Cuenta administradora DEMO creada. Cambiar credenciales antes de desplegar.");
        }
        seedStaff(salesEmail, salesPassword, "SALES_STAFF");
        seedStaff(inventoryEmail, inventoryPassword, "INVENTORY_MANAGER");
        add("LAP-ASPIRE-16", "Laptop Horizon 16 Ryzen 7", "Horizon", "Laptops", "3299.90", 8, "Laptop para productividad y programacion", Map.of("RAM", "16GB", "SSD", "512GB", "CPU", "Ryzen 7"));
        add("LAP-NOVA-14", "Ultrabook Nova 14 Core i5", "Nova", "Laptops", "2599.00", 12, "Equipo liviano para estudio y trabajo", Map.of("RAM", "16GB", "SSD", "1TB", "Pantalla", "14 pulgadas"));
        add("MON-ORBIT-27", "Monitor Orbit 27 IPS 165 Hz", "Orbit", "Monitores", "899.90", 15, "Monitor ideal para escritorio", Map.of("Panel", "IPS", "Hz", "165", "Resolucion", "QHD"));
        add("MON-VIEW-24", "Monitor View 24 Full HD", "View", "Monitores", "469.00", 22, "Pantalla de trabajo y estudio", Map.of("Panel", "IPS", "Hz", "75", "Resolucion", "FHD"));
        add("KEY-MECH-01", "Teclado mecanico RGB TKL", "KeyForge", "Perifericos", "199.90", 31, "Teclado compacto y resistente", Map.of("Switch", "Red", "Formato", "TKL", "Conexion", "USB"));
        add("MOU-WIRE-01", "Mouse inalambrico Precision", "KeyForge", "Perifericos", "89.90", 40, "Mouse ergonomico de precision", Map.of("DPI", "16000", "Conexion", "2.4GHz"));
        add("SSD-1T-01", "SSD NVMe 1 TB Gen4", "FlashCore", "Componentes", "349.00", 26, "Almacenamiento rapido", Map.of("Capacidad", "1TB", "Interfaz", "NVMe", "Generacion", "4"));
        add("RAM-DDR5-32", "Memoria DDR5 32 GB Kit", "ByteMax", "Componentes", "529.00", 16, "Kit de memoria de alto rendimiento", Map.of("RAM", "32GB", "Tipo", "DDR5", "MHz", "6000"));
        add("GPU-ARC-12", "Tarjeta grafica Arc 12 GB", "PixelArc", "Componentes", "1699.00", 5, "Grafica dedicada para creacion de contenido", Map.of("VRAM", "12GB", "Conexion", "PCIe"));
        add("HDP-ANC-01", "Audifonos Studio ANC", "Sonic", "Audio", "299.90", 20, "Audio envolvente y cancelacion activa", Map.of("Conexion", "Bluetooth", "ANC", "Si"));
        add("CAM-WEB-02", "Webcam Vision Full HD", "View", "Perifericos", "159.90", 18, "Camara para reuniones y streaming", Map.of("Resolucion", "1080p", "FPS", "60"));
        add("TAB-NOVA-11", "Tablet Nova 11 Wi-Fi", "Nova", "Tablets", "1199.00", 10, "Tablet para notas, lectura y entretenimiento", Map.of("Pantalla", "11 pulgadas", "RAM", "8GB"));
    }

    /** Usuarios de desarrollo. Deshabilitar SEED_DEMO y cambiar credenciales en entornos reales. */
    private void seedStaff(String email, String password, String role) {
        String normalized = email.strip().toLowerCase(java.util.Locale.ROOT);
        if (users.existsByEmail(normalized)) return;
        AppUser staff = new AppUser();
        staff.email = normalized;
        staff.passwordHash = encoder.encode(password);
        staff.role = role;
        users.save(staff);
        log.warn("Usuario DEMO {} creado: {}", role, normalized);
    }

    private void add(String sku, String name, String brand, String category, String price, int stock, String desc, Map<String, String> specs) {
        if (products.existsBySku(sku)) return;
        Product p = new Product();
        p.sku = sku;
        p.name = name;
        p.brand = brand;
        p.category = category;
        p.price = new BigDecimal(price);
        p.stock = stock;
        p.active = true;
        p = products.save(p);
        try {
            details.save(new ProductDetails(p.id, desc, specs));
        }
        catch (Exception ex) {
            log.warn("MongoDB no disponible para semilla {}: {}", sku, ex.getMessage());
        }
    }
}
