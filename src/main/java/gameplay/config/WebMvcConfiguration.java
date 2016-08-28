package gameplay.config;

import gameplay.interceptors.RequestInterceptor;
import org.apache.catalina.security.SecurityConfig;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.*;

import java.util.List;

/**
 * Created by shubham.singhal on 26/08/16.
 */
@EnableWebMvc
@Configuration
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {


    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**");
            }
        };
    }

    @Bean
    RequestInterceptor requestInterceptor() {
        return new RequestInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestInterceptor()).addPathPatterns("/**");
    }
}
