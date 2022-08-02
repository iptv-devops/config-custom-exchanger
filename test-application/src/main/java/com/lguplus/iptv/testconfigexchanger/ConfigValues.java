package com.lguplus.iptv.testconfigexchanger;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "test")
@Data
public class ConfigValues {

    private Map<String, String> values = new HashMap<>();

}
