package com.nokhrin.accounting.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nokhrin.accounting.domain.account.AccountRepository;
import com.nokhrin.accounting.domain.event.EventRepository;
import com.nokhrin.accounting.infrastructure.persistence.JdbcAccountRepository;
import com.nokhrin.accounting.infrastructure.persistence.JdbcEventRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class Config {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

    @Bean
    public AccountRepository accountRepository(DataSource dataSource) {
        return new JdbcAccountRepository(dataSource);
    }

    @Bean
    public EventRepository eventRepository(DataSource dataSource, ObjectMapper objectMapper) {
        return new JdbcEventRepository(dataSource, objectMapper);
    }
}
