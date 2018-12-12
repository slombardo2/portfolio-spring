/*
       Copyright 2018 IBM Corp All Rights Reserved
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.ibm.hybrid.cloud.sample.portfolio.jwt;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.DelegatingJwtClaimsSetVerifier;
import org.springframework.security.oauth2.provider.token.store.IssuerClaimVerifier;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtClaimsSetVerifier;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

@Configuration
@EnableResourceServer //relies on @EnableWebSecurity over in SecurityConfig
public class JwtResourceConfig extends ResourceServerConfigurerAdapter {

    @Value("${jwt.audience}")
    private String audience;   

    @Value("${jwt.issuer}")
    private String issuer;  

    @Autowired
    JwtRolesTokenConverter jwtRolesConverter;

    @Autowired
    JwtAuthenticationEntryPoint jwtAuthEntryPoint;

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        DefaultAccessTokenConverter defaultConverter = new DefaultAccessTokenConverter();
        defaultConverter.setUserTokenConverter(jwtRolesConverter);

        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();

        //TODO: read from config / environment.
        KeyStoreKeyFactory keyStoreKeyFactory =
                new KeyStoreKeyFactory(
                        new ClassPathResource("key.jks"),
                        "passw0rd".toCharArray());
        converter.setKeyPair(keyStoreKeyFactory.getKeyPair("default"));

        converter.setAccessTokenConverter(defaultConverter);
        converter.setJwtClaimsSetVerifier(jwtClaimsSetVerifier());
        return converter;
    }	

    @Bean
    @Primary
    public DefaultTokenServices tokenServices() {
        JwtTokenService tokenService = new JwtTokenService();
        tokenService.setTokenStore(tokenStore());
        tokenService.setSupportRefreshToken(false); //rs doesn't do refresh

        tokenService.setTokenEnhancer(accessTokenConverter());
        return tokenService;
    }	

    @Bean
    public JwtClaimsSetVerifier jwtClaimsSetVerifier() {
        return new DelegatingJwtClaimsSetVerifier(Arrays.asList(
                         issuerClaimVerifier(),
                         requiredClaimsVerifier()
                         ));
    }

    @Bean
    public JwtClaimsSetVerifier issuerClaimVerifier() {
        try {
            return new IssuerClaimVerifier(new URL(issuer));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public class RequireIssAudClaimVerifier implements JwtClaimsSetVerifier {
        @Override
        public void verify(Map<String, Object> claims) throws InvalidTokenException {
            String iss = (String) claims.get("iss");
            if ((iss == null) || (iss.length() == 0)) {
                throw new InvalidTokenException("iss claim is empty");
            }
            String aud = (String) claims.get("aud");
            if ((aud == null) || (aud.length() == 0)) {
                throw new InvalidTokenException("aud claim is empty");
            }            
        }
    }   

    @Bean JwtClaimsSetVerifier requiredClaimsVerifier() {
        return new RequireIssAudClaimVerifier();
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources
                .authenticationEntryPoint(jwtAuthEntryPoint)
                .resourceId(audience) //Spring verification of the `aud` claim.
                .tokenServices(tokenServices())
                .tokenStore(tokenStore());
    }


}