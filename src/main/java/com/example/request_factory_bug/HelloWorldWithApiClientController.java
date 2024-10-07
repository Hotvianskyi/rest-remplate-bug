package com.example.request_factory_bug;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@RestController
public class HelloWorldWithApiClientController {
    private static final Logger log = LoggerFactory.getLogger(HelloWorldWithApiClientController.class);

    /**
     * Combination of Having an Interceptor, Wrapping SimpleClientHttpRequestFactory with BufferingClientHttpRequestFactory, and sending a
     * POST request result in IOException instead of properly managed 401 response
     * */
    @RequestMapping(
          method = RequestMethod.GET,
          value = "hello-buffer-simple-intercept",
          produces = { "text/plain" }
    )
    public ResponseEntity<String> helloWorldBufferSimpleInterceptor() {
        try {
            RestTemplate restTemplate = buildRestTemplate();
            restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
            restTemplate.getInterceptors().add(new LoggingInterceptor());
            var request = new RequestEntity<String>(HttpMethod.POST, URI.create("http://localhost:8800/hello-world-401"));
            var response = restTemplate.exchange(request, String.class);
            log.info("Logged Response helloWorldBufferSimpleInterceptor: {}", response.getBody());
            return ResponseEntity.ok(response.getBody() + " SimpleClientHttpRequestFactory");
        } catch (Exception e) {
            log.error("Logged error: SimpleClientHttpRequestFactory + Interceptor", e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    protected RestTemplate buildRestTemplate() {
        var restTemplate = new RestTemplate();
        // disable default URL encoding
        var uriBuilderFactory = new DefaultUriBuilderFactory();
        uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
        restTemplate.setUriTemplateHandler(uriBuilderFactory);
        return restTemplate;
    }

    /**
     * Combination of Wrapping SimpleClientHttpRequestFactory with BufferingClientHttpRequestFactory and sending a
     * POST request results 401 without a body
     * */
    @RequestMapping(
          method = RequestMethod.GET,
          value = "hello-buffer-simple",
          produces = { "text/plain" }
    )
    public ResponseEntity<String> helloWorldBufferSimple() {
        try {
            RestTemplate restTemplate = buildRestTemplate();
            restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
            var request = new RequestEntity<String>(HttpMethod.POST, URI.create("http://localhost:8800/hello-world-401"));
            var response = restTemplate.exchange(request, String.class);
            log.info("Logged Response helloWorldBufferSimple: {}", response.getBody());
            return ResponseEntity.ok(response.getBody() + " SimpleClientHttpRequestFactory");
        } catch (Exception e) {
            log.error("Logged error: SimpleClientHttpRequestFactory", e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }


    /**
     * Combination of Wrapping HttpComponentsClientHttpRequestFactory with BufferingClientHttpRequestFactory and sending a
     * POST request results 401 with body 'Unauthorized'
     * */
    @RequestMapping(
          method = RequestMethod.GET,
          value = "hello-buffer-httpcomponents",
          produces = { "text/plain" }
    )
    public ResponseEntity<String> helloWorldBufferHttpComponents() {
        try {
            RestTemplate restTemplate = buildRestTemplate();
            //Wrapped request factory
            restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(new HttpComponentsClientHttpRequestFactory()));
            //Interceptor
            restTemplate.getInterceptors().add(new LoggingInterceptor());
            var request = new RequestEntity<String>(HttpMethod.POST, URI.create("http://localhost:8800/hello-world-401"));
            var response = restTemplate.exchange(request, String.class);
            log.info("Logged Response helloWorldBufferHttpComponents: {}", response.getBody());
            return ResponseEntity.ok(response.getBody() + " HttpComponentsClientHttpRequestFactory");
        } catch (Exception e) {
            log.error("Logged error: HttpComponentsClientHttpRequestFactory", e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }

    }

    @RequestMapping(
          method = RequestMethod.POST,
          value = "hello-world-401",
          produces = { "text/plain" }
    )
    public ResponseEntity<String> helloWorldUnauth() {
        return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
    }

    private static class LoggingInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            var response = execution.execute(request, body);
            // Interceptor tries to log the response body. If buffering does not work as intended, this throws an
            // IOException because errorStream field inside SimpleClientHttpResponse is null
            log.info(bodyToString(response.getBody()));
            log.info(response.getStatusCode().toString());
            return response;
        }

        private String bodyToString(InputStream body) throws IOException {
            StringBuilder builder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(body, StandardCharsets.UTF_8));
            String line = bufferedReader.readLine();
            while (line != null) {
                builder.append(line).append(System.lineSeparator());
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
            return "Logged body:" + builder;
        }
    }
}
