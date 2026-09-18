package br.com.smartcollector.admin.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Datasource transacional (Oracle). E o @Primary: qualquer @Transactional
 * sem qualificador cai aqui, que e o comportamento seguro por padrao.
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "br.com.smartcollector.admin.repository",
        entityManagerFactoryRef = "oracleEntityManagerFactory",
        transactionManagerRef = "oracleTransactionManager")
public class OracleDataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("app.datasource.oracle")
    public DataSourceProperties oracleDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("app.datasource.oracle.hikari")
    public DataSource oracleDataSource(
            @Qualifier("oracleDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    /**
     * Com dois datasources a autoconfiguracao do Spring Boot fica desligada,
     * entao este builder precisa ser declarado manualmente.
     */
    @Bean
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder() {
        return new EntityManagerFactoryBuilder(
                new HibernateJpaVendorAdapter(),
                new HashMap<>(),
                null);
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean oracleEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("oracleDataSource") DataSource dataSource) {

        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.dialect", "org.hibernate.dialect.OracleDialect");
        props.put("hibernate.hbm2ddl.auto", "validate");
        props.put("hibernate.jdbc.batch_size", 25);

        return builder.dataSource(dataSource)
                .packages("br.com.smartcollector.admin.model")
                .persistenceUnit("oracle")
                .properties(props)
                .build();
    }

    @Bean
    @Primary
    public PlatformTransactionManager oracleTransactionManager(
            @Qualifier("oracleEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean(initMethod = "migrate")
    public Flyway oracleFlyway(@Qualifier("oracleDataSource") DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/oracle")
                .baselineOnMigrate(true)
                .load();
    }
}