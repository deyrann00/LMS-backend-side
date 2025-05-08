package com.example.lms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // CORS Configuration
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000")
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowCredentials(true);
            }
        };
    }

    // Resource Handler Configuration to Serve Static Files
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ensure to replace this path with the correct absolute path to your 'uploads' folder
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations("file:uploads/avatars/");
    }
}