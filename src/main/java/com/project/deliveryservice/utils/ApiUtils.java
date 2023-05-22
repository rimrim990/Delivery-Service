package com.project.deliveryservice.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

public class ApiUtils {

    @Getter
    @AllArgsConstructor
    public static class ApiResponse<T> {
        private T data;
        private List<String> errorMsg;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, null);
    }


    public static ApiResponse fail(String errorMsg) {
        return new ApiResponse(null, List.of(errorMsg));
    }

    public static ApiResponse fail(Throwable throwable) {
        return fail(throwable.getMessage());
    }

    public static ApiResponse fail(List<String> errorMsg) {
        return new ApiResponse(null, errorMsg);
    }
}
