package com.lguplus.iptv.configexchanger;

import java.util.Optional;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.lang.NonNull;

@ConfigureExchanger
public abstract class ConfigurationExchanger {

    public static final String CONFIG_PREFIX = "config-exchanger";

    public abstract boolean isMatchedItem(String value);

    @NonNull
    public abstract String exchange(String value);

    public abstract String exchangerName();

    protected String getConfigParamKey(String key) {
        return CONFIG_PREFIX + "." + exchangerName() + "." + key;
    }

    public boolean enable(ConfigurableEnvironment environment) {
        return Optional.of(Boolean.valueOf(
                environment.getProperty(getConfigParamKey("enabled"))))
            .orElse(false);
    }
}
