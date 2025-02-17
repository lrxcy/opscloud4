package com.baiyi.opscloud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@Configuration
@EnableSwagger2
public class SwaggerConfigurer {

    /**
     * https://doc.xiaominfo.com/knife4j/documentation/
     */

    @Bean
    public Docket openAPI() {
        return new Docket(DocumentationType.SWAGGER_2)
                .forCodeGeneration(true)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.baiyi.opscloud.controller"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());
    }

    //API 作者相关信息
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("OPSCLOUD (IaC) 4.0.0")
                .description("OPSCLOUD OPENAPI接口文档")
                .version("4.0.0-RELEASE")
                .license("GNU General Public License v2")
                .licenseUrl("https://www.gnu.org/licenses/old-licenses/gpl-2.0.html")
                .contact(new Contact("白衣", "https://github.com/ixrjog/opscloud", "ixrjog@qq.com"))
                .build();
    }


}