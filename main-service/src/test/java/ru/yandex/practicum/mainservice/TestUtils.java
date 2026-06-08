package ru.yandex.practicum.mainservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MvcResult;

public final class TestUtils {

    private TestUtils() {
    }

    public static String asJsonString(ObjectMapper objectMapper, Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Long extractId(ObjectMapper objectMapper, MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsByteArray())
                .get("id")
                .asLong();
    }
}
