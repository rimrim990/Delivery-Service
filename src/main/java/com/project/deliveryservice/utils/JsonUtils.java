package com.project.deliveryservice.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JsonUtils {

    private final ObjectMapper objectMapper;

    public <T> String serialize(T data) throws JsonProcessingException {
        return objectMapper.writeValueAsString(data);
    }

    public <T> T deserialize(String json, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(json, clazz);
    }

    public <T> T deserialize(String json, JavaType type) throws JsonProcessingException {
        return objectMapper.readValue(json, type);
    }

    public <T, K> JavaType getParametricType(Class<T> wrapper, Class<K> parameter) {
       return  objectMapper.getTypeFactory()
               .constructParametricType(wrapper, parameter);
    }
}
