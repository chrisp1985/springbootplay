package com.chrisp1985.springbootplay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableCaching
public class SpringbootplayApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootplayApplication.class, args);
	}

}
