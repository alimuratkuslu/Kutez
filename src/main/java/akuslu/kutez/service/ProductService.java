package akuslu.kutez.service;

import akuslu.kutez.dto.GoldPriceResponse;
import akuslu.kutez.model.Product;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private List<Product> products = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RestTemplate restTemplate;

    public ProductService() {
        this.restTemplate = new RestTemplate();
    }

    @PostConstruct
    public void init() throws IOException {
        try {
            ClassPathResource resource = new ClassPathResource("products.json");
            products = objectMapper.readValue(resource.getInputStream(),
                    new TypeReference<List<Product>>() {});

            products.forEach(this::calculatePrice);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load product data", e);
        }
    }

    @Value("${goldapi.key}")
    private String goldApiKey;
    private static final String GOLD_API_URL = "https://www.goldapi.io/api/XAU/USD";

    public List<Product> getAllProducts() {
        return products;
    }

    public List<Product> getFilteredProducts(Double minPrice, Double maxPrice,
                                             Double minPopularity, Double maxPopularity) {
        return products.stream()
                .filter(product -> {
                    boolean matches = true;

                    if (minPrice != null) {
                        matches = matches && product.getPrice() >= minPrice;
                    }
                    if (maxPrice != null) {
                        matches = matches && product.getPrice() <= maxPrice;
                    }
                    if (minPopularity != null) {
                        double starRating = convertToStarRating(product.getPopularityScore());
                        matches = matches && starRating >= minPopularity;
                    }
                    if (maxPopularity != null) {
                        double starRating = convertToStarRating(product.getPopularityScore());
                        matches = matches && starRating <= maxPopularity;
                    }

                    return matches;
                })
                .collect(Collectors.toList());
    }

    public Product calculatePrice(Product product) {
        double goldPrice = fetchGoldPrice();
        double goldPricePerGram = goldPrice / 31.1035;

        product.setPrice((product.getPopularityScore() + 1) * product.getWeight() * goldPricePerGram);

        return product;
    }

    private double convertToStarRating(int popularityScore) {
        BigDecimal rating = BigDecimal.valueOf(popularityScore)
                .multiply(BigDecimal.valueOf(5))
                .divide(BigDecimal.valueOf(100), 1, BigDecimal.ROUND_HALF_UP);
        return rating.doubleValue();
    }

    private double fetchGoldPrice() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-access-token", goldApiKey);
        headers.set("Content-Type", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<GoldPriceResponse> response = restTemplate.exchange(
                    GOLD_API_URL,
                    HttpMethod.GET,
                    entity,
                    GoldPriceResponse.class
            );

            if (response.getBody() != null) {
                return response.getBody().getPrice_gram_24k();
            }

            return 2000.0;
        } catch (Exception e) {
            System.err.println("Error fetching gold price: " + e.getMessage());
            return 2000.0;
        }
    }
}
