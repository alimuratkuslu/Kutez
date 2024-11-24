package akuslu.kutez.controller;

import akuslu.kutez.model.Product;
import akuslu.kutez.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/product")
@RestController
@CrossOrigin(origins = "https://akuslu-kutez-frontend-5c2424ea44ae.herokuapp.com")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minPopularity,
            @RequestParam(required = false) Double maxPopularity
    ) {
        return ResponseEntity.ok(productService.getFilteredProducts(minPrice, maxPrice, minPopularity, maxPopularity));
    }
}
