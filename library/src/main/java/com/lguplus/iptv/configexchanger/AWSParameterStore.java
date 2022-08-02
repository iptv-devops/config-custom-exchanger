package com.lguplus.iptv.configexchanger;

import java.util.Objects;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.env.ConfigurableEnvironment;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.SsmClientBuilder;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;

@ConditionalOnClass(ConfigPostProcessor.class)
public class AWSParameterStore extends ConfigurationExchanger {

    public static final String SSM_PROPERTY_PREFIX = "{aws_parameter_store}";

    private static final String EMPTY_STRING = "";

    private SsmClient ssmClient;

    @Override
    public boolean isMatchedItem(String value) {
        Objects.requireNonNull(value);
        return value.startsWith(SSM_PROPERTY_PREFIX);
    }

    @Override
    public String exchange(String value) {
        GetParameterResponse response = ssmClient.getParameter(
            GetParameterRequest.builder().name(value.replace(SSM_PROPERTY_PREFIX, ""))
                .withDecryption(true).build());

        return response.parameter().value();
    }

    @Override
    public String exchangerName() {
        return "aws-parameter-store";
    }

    @Override
    public boolean enable(ConfigurableEnvironment environment) {
        boolean enabled = super.enable(environment);
        if (enabled) {
            initialize(environment);
            return true;
        }
        return false;
    }

    private void initialize(ConfigurableEnvironment environment) {
        SsmClientBuilder builder = SsmClient.builder();

        String region = Objects.requireNonNullElse(
            environment.getProperty(getConfigParamKey("region")), EMPTY_STRING);
        if (!EMPTY_STRING.equals(region)) {
            builder.region(Region.of(region));
        }

        ssmClient = builder.build();
    }

}
