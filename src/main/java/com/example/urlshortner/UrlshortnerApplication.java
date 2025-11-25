package com.example.urlshortner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
<<<<<<< HEAD
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
=======
>>>>>>> c8a73bf542437b4a82eb4ae49705ad7fcf75a231
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
<<<<<<< HEAD
public class UrlshortnerApplication  extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(UrlshortnerApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(UrlshortnerApplication.class);
	}
=======
public class UrlshortnerApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrlshortnerApplication.class, args);
    }
>>>>>>> c8a73bf542437b4a82eb4ae49705ad7fcf75a231

}
