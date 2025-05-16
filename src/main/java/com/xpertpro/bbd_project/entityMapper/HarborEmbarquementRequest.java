package com.xpertpro.bbd_project.entityMapper;

import lombok.Data;

import java.util.List;

@Data
public class HarborEmbarquementRequest {
    private Long harborId;
    private List<Long> containerId;
}
