package com.clinic.config;

import jakarta.servlet.RequestDispatcher;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true);
    }

    @Bean
    public ErrorViewResolver customErrorViewResolver() {
        return (request, status, model) -> {
            if (status == HttpStatus.NOT_FOUND) {
                String path = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
                if (path != null &&
                    !path.startsWith("/api") &&
                    !path.startsWith("/actuator") &&
                    !path.startsWith("/swagger-ui") &&
                    !path.startsWith("/v3/api-docs")) {
                    return new ModelAndView("forward:/index.html", HttpStatus.OK);
                }
            }
            return null;
        };
    }
}
