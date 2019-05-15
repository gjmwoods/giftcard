package io.axoniq.demo.giftcard;

import org.axonframework.common.jdbc.PersistenceExceptionResolver;
import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.common.jpa.SimpleEntityManagerProvider;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.eventsourcing.eventstore.jpa.SQLErrorCodesResolver;
import org.axonframework.spring.messaging.unitofwork.SpringTransactionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.transaction.PlatformTransactionManager;

import java.sql.SQLException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
public class EphemeralEventStoreConfig {


    @Bean
    @Qualifier("ephemeral")
    @ConfigurationProperties(prefix = "ephemeral.datasource")
    public DataSource ephemeralDataSource() {
        return DataSourceBuilder.create().build();
    }


    @Bean
    @Qualifier("ephemeral")
    @ConfigurationProperties(prefix = "ephemeral.jpa")
    public JpaProperties ephemeralEventsJpaProperties() {
        return new JpaProperties();
    }


    @Bean
    @Qualifier("ephemeral")
    public LocalContainerEntityManagerFactoryBean ephemeralEventsEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("ephemeral") DataSource dataSource,
            @Qualifier("ephemeral") JpaProperties jpaProperties) {
        return builder
                .dataSource(dataSource)
                .properties(jpaProperties.getProperties())
                .packages("org.axonframework.eventsourcing.eventstore.jpa")
                .persistenceUnit("events")
                .build();
    }

    @Bean
    @Qualifier("ephemeral")
    public PlatformTransactionManager ephemeralEventsPlatformTransactionManager(
            @Qualifier("ephemeral") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }


    @Bean
    @Qualifier("ephemeral")
    public EntityManager ephemeralEventsSharedEntityManager(    @Qualifier("ephemeral") EntityManagerFactory entityManagerFactory) {
        return SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory);
    }

    //Axon specific configuration
    @Bean
    @Qualifier("ephemeral")
    public EntityManagerProvider ephemeralEventsEntityManagerProvider(@Qualifier("ephemeral") EntityManager entityManager) {
        return new SimpleEntityManagerProvider(entityManager);
    }

    @Bean
    @Qualifier("ephemeral")
    public TransactionManager ephemeralEventsTransactionManager(@Qualifier("ephemeral") PlatformTransactionManager transactionManager) {
        return new SpringTransactionManager(transactionManager);
    }

    @Bean
    @Qualifier("ephemeral")
    public PersistenceExceptionResolver ephemeralEventsDataSourcePER(
            @Qualifier("ephemeral") DataSource dataSource) throws SQLException {
        return new SQLErrorCodesResolver(dataSource);
    }


}