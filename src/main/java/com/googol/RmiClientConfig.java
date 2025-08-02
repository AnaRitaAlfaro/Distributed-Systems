package com.googol;

import com.googol.rmi.Gateway_I;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.rmi.Naming;

@Configuration
public class RmiClientConfig {

    @Bean
    public Gateway_I gateway() throws Exception {
        return (Gateway_I) Naming.lookup("rmi://localhost:1099/Gateway_I");
    }
}
