package com.budgetserver;

import com.budgetserver.service.BudgetMcpService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class BudgetMcpServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(BudgetMcpServerApplication.class, args);
	}

	@Bean
	public ToolCallbackProvider budgetTools(BudgetMcpService budgetMcpService) {
		return MethodToolCallbackProvider.builder()
				.toolObjects(budgetMcpService)
				.build();
	}
}
