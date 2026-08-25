package com.example.springboottemplate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"wechat.pay.mock=true"
})
class SpringbootTemplateApplicationTests {

	@Test
	void contextLoads() {
	}

}
