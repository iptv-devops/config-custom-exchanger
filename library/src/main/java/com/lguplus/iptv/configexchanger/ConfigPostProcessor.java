package com.lguplus.iptv.configexchanger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.cglib.beans.BeanGenerator;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.type.filter.AnnotationTypeFilter;

@Configuration
public class ConfigPostProcessor implements EnvironmentPostProcessor {

    private static final String CONFIG_SERVER_SOURCE_PREFIX = "configserver:";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
                                       SpringApplication application) {

        List<ConfigurationExchanger> policies = getPolicies(environment);
        MutablePropertySources mutablePropertySources = environment.getPropertySources();
        for (PropertySource<?> propertySource : mutablePropertySources) {
            if (propertySource.getName().startsWith(CONFIG_SERVER_SOURCE_PREFIX)) {

                Map<String, String> source = (Map<String, String>) propertySource.getSource();
                source.keySet().forEach(key -> policies.forEach(policy -> {
                    String originValue = String.valueOf(source.get(key));
                    if (policy.isMatchedItem(originValue)) {
                        String exchangedValue = policy.exchange(originValue);
                        source.put(key, exchangedValue);
                    }
                }));
            }
        }
    }

    private List<ConfigurationExchanger> getPolicies(ConfigurableEnvironment environment) {
        List<ConfigurationExchanger> exchangers = new ArrayList<>();

        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(true);

        scanner.addIncludeFilter(new AnnotationTypeFilter(ConfigureExchanger.class));

        try {
            for (BeanDefinition bd : scanner.findCandidateComponents(
                    "com.lguplus.iptv.configexchanger")) {
                Class<?> c = BeanGenerator.class.getClassLoader().loadClass(bd.getBeanClassName());
                Constructor<?> con = c.getConstructor();
                Object o = con.newInstance();
                if (o instanceof ConfigurationExchanger) {
                    ConfigurationExchanger configurationExchanger = ((ConfigurationExchanger) o);
                    if (configurationExchanger.enable(environment)) {
                        exchangers.add(configurationExchanger);
                    }
                }
            }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return exchangers;
    }

}

