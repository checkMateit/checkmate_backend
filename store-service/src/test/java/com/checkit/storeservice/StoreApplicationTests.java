package com.checkit.storeservice;

import com.checkit.storeservice.config.TestRabbitConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestRabbitConfig.class)
class StoreApplicationTests {

	@Test
	void contextLoads() {
	}
}
