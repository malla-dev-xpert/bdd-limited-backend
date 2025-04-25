package com.xpertpro.bbd_project.dto.harbor;

import com.xpertpro.bbd_project.dto.containers.ContainersDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor
public class HarborDto {
    private Long id;
    private String name;
    private String location;
    private List<ContainersDto> containers;
    private Long userid;
    private String userName;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime editedAt = LocalDateTime.now();
}
