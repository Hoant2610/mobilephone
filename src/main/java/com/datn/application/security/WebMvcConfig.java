package com.datn.application.security;

import java.time.Duration;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import java.util.concurrent.*;


@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {
    private long cacheDurationInSeconds = 2 * 60 * 60;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Cho phép tất cả các đường dẫn
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("authorization", "content-type", "x-auth-token")
                .exposedHeaders("x-auth-token")
                .allowCredentials(false).maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ConfigInterceptor())
                .excludePathPatterns("/image/**", "/vendor/**", "/css/**", "/script/**", "/api/**", "/api/register", "/favicon.ico","/shop/**", "/adminlte/**", "/media/static/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/image/**").addResourceLocations("classpath:/static/image/")
                .setCacheControl(CacheControl.maxAge(Duration.ofSeconds(cacheDurationInSeconds)).cachePublic());
        registry.addResourceHandler("/vendor/**").addResourceLocations("classpath:/static/vendor/")
                .setCacheControl(CacheControl.maxAge(Duration.ofSeconds(cacheDurationInSeconds)).cachePublic());
        registry.addResourceHandler("/script/**").addResourceLocations("classpath:/static/script/")
                .setCacheControl(CacheControl.maxAge(Duration.ofSeconds(cacheDurationInSeconds)).cachePublic());
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/")
                .setCacheControl(CacheControl.maxAge(Duration.ofSeconds(cacheDurationInSeconds)).cachePublic());
        registry.addResourceHandler("/adminlte/**").addResourceLocations("classpath:/static/adminlte/")
                .setCacheControl(CacheControl.maxAge(Duration.ofSeconds(cacheDurationInSeconds)).cachePublic());
        registry.addResourceHandler("/shop/**").addResourceLocations("classpath:/static/shop/")
                .setCacheControl(CacheControl.maxAge(Duration.ofSeconds(cacheDurationInSeconds)).cachePublic());
    }
}
