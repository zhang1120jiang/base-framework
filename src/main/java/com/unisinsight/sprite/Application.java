/*
 * www.unisinsight.com Inc.
 * Copyright (c) 2018 All Rights Reserved
 */
package com.unisinsight.sprite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * description
 *
 * @author t17153 [tan.gang@h3c.com]
 * @date 2018/09/13 21:51
 * @since 1.0
 */
@EnableSwagger2
@SpringBootApplication
@EnableCaching
public class Application {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(Application.class, args);

        // 针对404错误，抛出异常
        DispatcherServlet dispatcherServlet = (DispatcherServlet) ctx.getBean("dispatcherServlet");
        dispatcherServlet.setThrowExceptionIfNoHandlerFound(true);
    }
}
