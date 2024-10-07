package com.example.request_factory_bug;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HelloWorldWithApiClientController.class)
class HelloWorldWithApiClientControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void helloWorldBufferSimpleInterceptor() throws Exception {
        mockMvc.perform(get("/hello-buffer-simple-intercept"))
              .andExpect(content().string("I/O error on POST request for \"http://localhost:8800/hello-world-401\": cannot retry due to server authentication, in streaming mode"));
    }

    @Test
    void helloWorldBufferSimple() throws Exception {
        mockMvc.perform(get("/hello-buffer-simple"))
              .andExpect(content().string("401 : [no body]"));
    }

    @Test
    void helloWorldBufferHttpComponents() throws Exception {
        mockMvc.perform(get("/hello-buffer-httpcomponents"))
              .andExpect(content().string("401 : \"Unauthorized\""));
    }
}