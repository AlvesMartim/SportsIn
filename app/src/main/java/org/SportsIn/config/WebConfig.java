package org.SportsIn.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173", "http://localhost:8080")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addViewControllers(@NonNull ViewControllerRegistry registry) {
        // Rediriger toutes les routes non-API vers index.html pour le routage React
        // Utilisation de noms de variables différents pour éviter l'erreur "capture twice"
        registry.addViewController("/{path:[\\w-]+}")
                .setViewName("forward:/index.html");
        registry.addViewController("/{path1:[\\w-]+}/{path2:[\\w-]+}")
                .setViewName("forward:/index.html");
        registry.addViewController("/{path1:[\\w-]+}/{path2:[\\w-]+}/{path3:[\\w-]+}")
                .setViewName("forward:/index.html");
    }
}

