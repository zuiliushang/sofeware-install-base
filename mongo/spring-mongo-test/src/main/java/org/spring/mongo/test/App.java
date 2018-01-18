package org.spring.mongo.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.mongodb.core.MongoTemplate;



@SpringBootApplication
public class App {
	
    @Autowired
    MongoTemplate mongoTemplate;
    
	public static void main(String[] args) {
		new SpringApplicationBuilder(App.class).web(true).run(args);
	}
	
}
