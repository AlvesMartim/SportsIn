package org.SportsIn.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration CORS pour permettre au frontend de communiquer avec le backend
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        String[] localOrigins = {
            "http://localhost:3000", "http://localhost:3001",
            "http://localhost:5173", "http://localhost:5174",
            "http://localhost:5175", "http://localhost:5176",
            "http://localhost:5177", "http://localhost:5178",
            "http://127.0.0.1:3000", "http://127.0.0.1:5173",
            "http://127.0.0.1:5174", "http://127.0.0.1:5175"
        };

        registry.addMapping("/api/**")
                .allowedOrigins(localOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);

        registry.addMapping("/**")
                .allowedOrigins(localOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
