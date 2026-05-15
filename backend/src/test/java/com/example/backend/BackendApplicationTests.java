package com.example.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@org.springframework.test.context.TestPropertySource(properties = {
		"APP_ENV=test",
		"APP_SECRET=0123456789abcdef0123456789abcdef"
})
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
