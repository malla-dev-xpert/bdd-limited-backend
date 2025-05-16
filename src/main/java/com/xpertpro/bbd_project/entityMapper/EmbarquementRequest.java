package com.xpertpro.bbd_project.entityMapper;

import lombok.Data;

import java.util.List;

@Data
public class EmbarquementRequest {
    private Long containerId;
    private List<Long> packageId;
}
