package org.spring.mongo.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=App.class)
public class MongoTest {
	
	@Autowired
	MongoTemplate mongoTemplate;
	
	@Test
	public void test01() {
		User user = new User("raindrops", "haha", 23);
		mongoTemplate.insert(user);
	}
	
	@Test
	public void test02() {
	    mongoTemplate.findAll(User.class).forEach(System.out::println);
	}
}

class User{
	private String name;
	
	private String info;
	
	private Integer age;
	
	

	public User() {
		super();
		// TODO Auto-generated constructor stub
	}

	public User(String name, String info, Integer age) {
		super();
		this.name = name;
		this.info = info;
		this.age = age;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

    @Override
    public String toString() {
        return "User [name=" + name + ", info=" + info + ", age=" + age + "]";
    }
	
	
}