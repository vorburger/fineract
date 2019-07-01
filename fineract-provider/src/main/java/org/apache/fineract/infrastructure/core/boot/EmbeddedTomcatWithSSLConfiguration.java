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

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Configuration
public class EmbeddedTomcatWithSSLConfiguration {

    // http://docs.spring.io/spring-boot/docs/1.1.5.RELEASE/reference/htmlsingle/#howto-enable-multiple-connectors-in-tomcat

    @Bean
    public TomcatServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.setContextPath(getContextPath());
        tomcat.addAdditionalTomcatConnectors(createSslConnector());
        return tomcat;
    }

    private String getContextPath() {
        return "/fineract-provider";
    }

    protected Connector createSslConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
        try {
            File keystore = getKeystore().getFile();
            File truststore = keystore;
            connector.setScheme("https");
            connector.setSecure(true);
            connector.setPort(getHTTPSPort());
            protocol.setSSLEnabled(true);
            protocol.setKeystoreFile(keystore.getAbsolutePath());
            protocol.setKeystorePass(getKeystorePass());
            protocol.setTruststoreFile(truststore.getAbsolutePath());
            protocol.setTruststorePass(getKeystorePass());
            // ? protocol.setKeyAlias("apitester");
            return connector;
        } catch (IOException ex) {
            throw new IllegalStateException("can't access keystore: [" + "keystore" + "] or truststore: [" + "keystore" + "]", ex);
        }
    }

    protected int getHTTPSPort() {
        // TODO This shouldn't be hard-coded here, but configurable
        return 8443;
    }

    protected String getKeystorePass() {
        return "openmf";
    }

    protected Resource getKeystore() {
        return new ClassPathResource("/keystore.jks");
    }
}
