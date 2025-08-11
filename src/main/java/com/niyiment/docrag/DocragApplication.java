package com.niyiment.docrag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class DocragApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocragApplication.class, args);
	}

}
