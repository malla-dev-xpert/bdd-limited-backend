package com.xpertpro.bbd_project.dto.achats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Data
@AllArgsConstructor @NoArgsConstructor
public class ConfirmItemsDelivery {
    @NotNull
    private Long userId;
    @NotNull
    private List<Long> itemIds;
}
