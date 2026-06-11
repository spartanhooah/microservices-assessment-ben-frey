package net.frey.orders.config;

import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "multitenancy")
public class MultitenantProperties {
    private String defaultTenant;
    private Map<String, DataSourceProperties> tenants;
}
