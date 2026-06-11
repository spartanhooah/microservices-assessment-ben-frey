package net.frey.orders.config.datasource;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.frey.orders.config.MultitenantProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataSourceConfig {
    private final MultitenantProperties multiTenantProperties;

    @Bean
    public DataSource dataSource() {
        Map<Object, Object> targetDataSources = new HashMap<>();

        multiTenantProperties.getTenants().forEach((tenantId, props) -> {
            assert props.getUrl() != null;
            assert props.getDriverClassName() != null;
            DataSource dataSource = DataSourceBuilder.create()
                    .url(props.getUrl())
                    .username(props.getUsername())
                    .password(props.getPassword())
                    .driverClassName(props.getDriverClassName())
                    .build();

            targetDataSources.put(tenantId, dataSource);
        });

        TenantAwareRoutingDataSource routingDataSource = new TenantAwareRoutingDataSource();
        routingDataSource.setTargetDataSources(targetDataSources);

        DataSource defaultDataSource = (DataSource) targetDataSources.get(multiTenantProperties.getDefaultTenant());

        routingDataSource.setDefaultTargetDataSource(defaultDataSource);

        return routingDataSource;
    }
}
