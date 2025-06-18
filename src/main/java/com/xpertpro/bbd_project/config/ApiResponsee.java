package com.xpertpro.bbd_project.config;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ApiResponsee<T> {
        private boolean success;
        private String message;
        private T data;
        private List<String> errors;
}
