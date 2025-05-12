package com.sadna_market.market.InfrastructureLayer;

import com.sadna_market.market.DomainLayer.*;

import lombok.NoArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Repository configuration class that provides bean definitions for all repositories.
 * This centralized configuration allows for easily switching repository implementations
 * in the future without changing dependent components.
 */

@Configuration
@NoArgsConstructor
public class RepositoryConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(RepositoryConfiguration.class);
    private String adminUsername = "admin";
    /**
     * Creates and configures the StoreRepository bean.
     * @return An implementation of IStoreRepository
     */
    @Bean
    public IStoreRepository storeRepository() {
        logger.info("Initializing Store Repository");
        return InMemoryStoreRepository.getInstance();
    }
    
    /**
     * Creates and configures the UserRepository bean.
     * @return An implementation of IUserRepository
     */
    @Bean
    public IUserRepository userRepository() {
        logger.info("Initializing User Repository");
        return InMemoryUserRepository.getInstance();
    }
    
    /**
     * Creates and configures the ProductRepository bean.
     * @return An implementation of IProductRepository
     */
    @Bean
    public IProductRepository productRepository() {
        logger.info("Initializing Product Repository");
        return InMemoryProductRepository.getInstance();
    }
    
    /**
     * Creates and configures the OrderRepository bean.
     * @return An implementation of IOrderRepository
     */
    @Bean
    public IOrderRepository orderRepository() {
        logger.info("Initializing Order Repository");
        return InMemoryOrderRepository.getInstance();
    }
    
    /**
     * Creates and configures the MessageRepository bean.
     * @return An implementation of IMessageRepository
     */
    @Bean
    public IMessageRepository messageRepository() {
        logger.info("Initializing Message Repository");
        return InMemoryMessageRepository.getInstance();
    }
    
    /**
     * Creates and configures the ReportRepository bean.
     * @return An implementation of IReportRepository
     */
    @Bean
    public IReportRepository reportRepository() {
        logger.info("Initializing Report Repository");
        return InMemoryReportRepository.getInstance();
    }

    /**
     * Returns the admin username according to this configuration.
     * @return The admin username
     */
    public String getAdminUsername() {
        return adminUsername;
    }


}