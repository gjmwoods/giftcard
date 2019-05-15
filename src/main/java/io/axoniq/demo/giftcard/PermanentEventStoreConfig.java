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
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.sql.SQLException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
public class PermanentEventStoreConfig {

    @Primary
    @Bean(name = "dataSource")
    @Qualifier("permanent")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }


    @Bean
    @Primary
    @Qualifier("permanent")
    @ConfigurationProperties(prefix = "spring.jpa")
    public JpaProperties eventsJpaProperties() {
        return new JpaProperties();
    }

    @Primary
    @Bean(name = "entityManagerFactory")
    @Qualifier("permanent")
    public LocalContainerEntityManagerFactoryBean eventsEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("dataSource") DataSource dataSource,
            @Qualifier("permanent") JpaProperties jpaProperties) {
        return builder
                .dataSource(dataSource)
                .properties(jpaProperties.getProperties())
                .packages("org.axonframework.eventsourcing.eventstore.jpa",
                          "org.axonframework.modelling.saga.repository.jpa",
                          "org.axonframework.eventhandling.tokenstore.jpa",
                          "io.axoniq.demo.giftcard.api")
                .persistenceUnit("events")
                .build();
    }

    @Bean
    @Qualifier("permanent")
    public PlatformTransactionManager eventsPlatformTransactionManager(
            @Qualifier("permanent") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Primary
    @Bean(name = "transactionManager")
    @Qualifier("permanent")
    public EntityManager eventsSharedEntityManager(    @Qualifier("permanent") EntityManagerFactory entityManagerFactory) {
        return SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory);
    }

    //Axon specific configuration
    @Bean
    @Qualifier("permanent")
    public EntityManagerProvider eventsEntityManagerProvider(@Qualifier("permanent") EntityManager entityManager) {
        return new SimpleEntityManagerProvider(entityManager);
    }

    @Bean
    @Primary
    @Qualifier("permanent")
    public TransactionManager eventsTransactionManager(@Qualifier("permanent") PlatformTransactionManager transactionManager) {
        return new SpringTransactionManager(transactionManager);
    }

    @Bean
    @Qualifier("permanent")
    public PersistenceExceptionResolver eventsDataSourcePER(
            @Qualifier("dataSource") DataSource dataSource) throws SQLException {
        return new SQLErrorCodesResolver(dataSource);
    }


}