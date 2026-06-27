package com.pessoal.galeria_ney.infra.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.url}")
    private String cloudinaryUrl;

    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        // O SDK do Cloudinary consegue extrair as credenciais diretamente desta URL
        config.put("CLOUDINARY_URL", cloudinaryUrl);
        return new Cloudinary(cloudinaryUrl);
    }
}