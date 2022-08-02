package com.lguplus.iptv.testconfigexchanger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.SsmClientBuilder;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class TestLibrary {

    private static final String CONFIG =
            "{\"name\":\"test-application\",\"profiles\":[\"default\"],\"label\":null,\"version\":null,\"state\":null,\"propertySources\":[{\"name\":\"file:/file.yml\",\"source\":{\"test.values.secret\":\"{aws_parameter_store}/config/ssm\"}}]}";

    @Value("${local.server.port}")
    protected int port;

    private static MockWebServer mockConfigServer;

    @Autowired
    private TestRestTemplate restTemplate;

    private static MockedStatic<SsmClient> SsmClientMock;

    @BeforeAll
    public static void beforeALl() throws IOException {
        SsmClientMock = mockStatic(SsmClient.class);
        AWSClientInit();

        mockConfigServer = new MockWebServer();
        mockConfigServer.start(8888);
        mockConfigServer.enqueue(new MockResponse()
                .setBody(CONFIG)
                .addHeader("Content-Type", "application/json"));
    }

    @AfterAll
    public static void afterALl() throws IOException {
        SsmClientMock.close();
        mockConfigServer.shutdown();
    }


    private static void AWSClientInit() {
        Parameter parameter = Mockito.mock(Parameter.class);
        when(parameter.value()).thenReturn("encrypted_param");

        GetParameterResponse getParameterResponse = Mockito.mock(GetParameterResponse.class);
        when(getParameterResponse.parameter()).thenReturn(parameter);

        SsmClient client = Mockito.mock(SsmClient.class);
        when(client.getParameter(Mockito.any(GetParameterRequest.class))).thenReturn(
                getParameterResponse);

        SsmClientBuilder builder = Mockito.mock(
                SsmClientBuilder.class);
        when(builder.region(Mockito.any(Region.class))).thenReturn(builder);
        when(builder.build()).thenReturn(client);

        when(SsmClient.builder()).thenReturn(builder);

    }

    @Test
    void testLoadNotEncryptedValue() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("http://localhost:" + port + "/values?key=plain",
                        String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(response.getBody(), "plain_value");

    }

    @Test
    void testLoadEncryptedValue() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("http://localhost:" + port + "/values?key=secret",
                        String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(response.getBody(), "encrypted_param");
    }
}
