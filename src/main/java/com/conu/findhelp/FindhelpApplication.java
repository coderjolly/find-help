package com.conu.findhelp;

import com.conu.findhelp.repositories.ManagerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class FindhelpApplication {


	@Autowired
	ManagerRepository groceryItemRepo;

	public static void main(String[] args) {
		SpringApplication.run(FindhelpApplication.class, args);
	}

}
