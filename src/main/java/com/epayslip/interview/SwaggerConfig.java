package com.epayslip.interview;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
	private static Logger LOG = LoggerFactory.getLogger(SwaggerConfig.class);

	@Bean
	public Docket apiInfo() {
		LOG.info("Initial SwaggerSpringMvcPlugin...");

		return new Docket(DocumentationType.SWAGGER_2).enable(true)
				.apiInfo(new ApiInfoBuilder().title("Epayslip REST API").description("This is Epayslip REST API Docs.")
						.contact(new Contact("R&D Team", "", "rd@epayslip.com")).license("Epayslip Internal Only").version("v1.0.0").build())
				.select().apis(RequestHandlerSelectors.basePackage("com.epayslip")).paths(PathSelectors.any()).build();
	}
}
