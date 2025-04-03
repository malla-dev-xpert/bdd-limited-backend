package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.PaymentDto;
import com.xpertpro.bbd_project.entity.Devises;
import com.xpertpro.bbd_project.entity.Partners;
import com.xpertpro.bbd_project.entity.Payments;
import com.xpertpro.bbd_project.entity.UserEntity;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.repository.DevisesRepository;
import com.xpertpro.bbd_project.repository.PartnerRepository;
import com.xpertpro.bbd_project.repository.PaymentRepository;
import com.xpertpro.bbd_project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class PaymentServices {
    @Autowired
    PartnerRepository partnerRepository;
    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    DevisesRepository devisesRepository;

    public String registerPayment(PaymentDto paymentsDto, Long userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Partners customer = partnerRepository.findById(paymentsDto.getCustomerId()).orElseThrow(() -> new RuntimeException("Customer not found"));
        Devises devises = devisesRepository.findById(paymentsDto.getDeviseId()).orElseThrow(() -> new RuntimeException("Devises not found"));

        Payments payment = new Payments();
        payment.setAmount(paymentsDto.getAmount());
        payment.setPaymentMethod(paymentsDto.getPaymentMethod());
        payment.setDevises(devises);
        payment.setCustomer(customer);
        payment.setCreatedAt(paymentsDto.getCreatedAt());
        payment.setStatus(StatusEnum.CREATE);
        payment.setUser(user);

        // Mise à jour du solde du client
        customer.setBalance(payment.getAmount() - customer.getBalance());
        partnerRepository.save(customer);

        paymentRepository.save(payment);
        return "SUCCESS";
    }

    public Page<Payments> getAllPayments(int page) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by("id").ascending());
        return paymentRepository.findByStatus(StatusEnum.CREATE, pageable);
    }
}
