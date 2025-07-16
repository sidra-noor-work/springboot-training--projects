package com.example.unit_testing;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
public class NewsApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testNewsGetSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/news/123"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.newsId", Matchers.is(123)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title", Matchers.is("title 123")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.details", Matchers.is("details 123")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.reportedBy", Matchers.is("reporter 123")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.reportedAt", Matchers.notNullValue()));
    }

    @Test
    public void testNewsGetNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/news/1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testNewsGetListSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/news"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)));
    }
}
