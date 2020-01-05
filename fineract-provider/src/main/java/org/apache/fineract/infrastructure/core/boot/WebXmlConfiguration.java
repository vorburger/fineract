/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.core.boot;

import java.util.logging.LogRecord;

import javax.servlet.Filter;
import javax.servlet.Servlet;

import org.apache.fineract.infrastructure.core.filters.ResponseCorsFilter;
import org.apache.fineract.infrastructure.security.filter.TenantAwareBasicAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletRegistrationBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.filter.DelegatingFilterProxy;

import com.sun.jersey.spi.container.servlet.WebComponent;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;

/**
 * This Configuration replaces what formerly was in web.xml.
 *
 * @see <a
 *      href="http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#howto-convert-an-existing-application-to-spring-boot">#howto-convert-an-existing-application-to-spring-boot</a>
 */
@Configuration
@Profile("basicauth")
public class WebXmlConfiguration {

    @Autowired
    private TenantAwareBasicAuthenticationFilter basicAuthenticationProcessingFilter;

    @Bean
    // Logging customization is required to avoid the following warning log which Jersey continously prints after FINERACT-726:
    // "A servlet request, to the URI https://localhost:8443/fineract-provider/api/v1/authentication?tenantIdentifier=default,
    // contains form parameters in the request body but the request body has been consumed by the servlet or a servlet filter
    // accessing the request parameters. Only resource methods using @FormParam will work as expected. Resource methods
    // consuming the request body by other means will not work as expected."
    public ServletRegistrationBean jersey() {
        Servlet jerseyServlet = new SpringServlet();
        ServletRegistrationBean jerseyServletRegistration = new ServletRegistrationBean();
        jerseyServletRegistration.setServlet(jerseyServlet);
        jerseyServletRegistration.addUrlMappings("/api/v1/*");
        jerseyServletRegistration.setName("jersey-servlet");
        jerseyServletRegistration.setLoadOnStartup(1);
        jerseyServletRegistration.addInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");
        jerseyServletRegistration.addInitParameter("com.sun.jersey.spi.container.ContainerResponseFilters",
                ResponseCorsFilter.class.getName());
        jerseyServletRegistration.addInitParameter("com.sun.jersey.config.feature.DisableWADL", "true");
        // debugging for development:
        // jerseyServletRegistration.addInitParameter("com.sun.jersey.spi.container.ContainerRequestFilters",
        // LoggingFilter.class.getName());
/*
        java.util.logging.Logger jerseyLogger = java.util.logging.Logger.getLogger(WebComponent.class.getName());
        jerseyLogger.setLevel(java.util.logging.Level.SEVERE);
        jerseyLogger.setFilter(new java.util.logging.Filter() {
            @Override
            public boolean isLoggable(LogRecord record) {
                boolean isLoggable = true;
                if (record.getMessage().contains("Only resource methods using @FormParam")) {
                    isLoggable = false;
                }
                return isLoggable;
            }
        });

        org.slf4j.bridge.SLF4JBridgeHandler.removeHandlersForRootLogger();
        org.slf4j.bridge.SLF4JBridgeHandler.install();
*/
        return jerseyServletRegistration;
    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(basicAuthenticationProcessingFilter);
        filterRegistrationBean.setEnabled(false);
        return filterRegistrationBean;
    }
}
