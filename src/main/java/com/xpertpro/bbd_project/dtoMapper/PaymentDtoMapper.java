package com.xpertpro.bbd_project.dtoMapper;

import com.xpertpro.bbd_project.dto.PaymentDto;
import com.xpertpro.bbd_project.entity.Payments;
import org.springframework.stereotype.Component;

@Component
public class PaymentDtoMapper {
    public Payments toEntity(PaymentDto dto) {
        Payments payments = new Payments();
        payments.setAmount(dto.getAmount());
        payments.setPaymentMethod(dto.getPaymentMethod());
        return payments;
    }
}
