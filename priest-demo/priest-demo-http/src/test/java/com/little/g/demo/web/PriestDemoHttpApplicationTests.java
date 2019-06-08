package com.little.g.demo.web;

import com.little.g.demo.api.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PriestDemoHttpApplicationTests {

	@Resource
	private UserService userService;

	@Test
	public void contextLoads() {
		userService.add(null);
	}

}
