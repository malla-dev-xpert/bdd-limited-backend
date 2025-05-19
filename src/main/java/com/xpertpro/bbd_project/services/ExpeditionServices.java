package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.ExpeditionDto;
import com.xpertpro.bbd_project.dtoMapper.ExpeditionDtoMapper;
import com.xpertpro.bbd_project.entity.Expeditions;
import com.xpertpro.bbd_project.entity.Partners;
import com.xpertpro.bbd_project.entity.UserEntity;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.repository.ExpeditionRepository;
import com.xpertpro.bbd_project.repository.PartnerRepository;
import com.xpertpro.bbd_project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpeditionServices {
    @Autowired
    ExpeditionRepository expeditionRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PartnerRepository clientRepo;
    @Autowired
    ExpeditionDtoMapper expeditionDtoMapper;

    public String create(ExpeditionDto dto, Long clientId, Long userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Partners client = clientRepo.findById(clientId).orElseThrow(() -> new RuntimeException("Client not found"));

        Expeditions expeditions = expeditionDtoMapper.toEntity(dto);

        expeditions.setCreatedAt(LocalDateTime.now());
        expeditions.setClient(client);
        expeditions.setCreatedBy(user);
        expeditionRepository.save(expeditions);
        return "SUCCESS";
    }

    public List<ExpeditionDto> getAll(int page, String query) {
        int pageSize = 30;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());

        Page<Expeditions> expeditions = expeditionRepository.findByStatus(StatusEnum.CREATE, pageable);

        if (query != null && !query.isEmpty()) {
            expeditions = expeditionRepository.findByStatusAndSearchQuery(
                    StatusEnum.CREATE,
                    "%" + query.toLowerCase() + "%",
                    pageable
            );
        } else {
            expeditions = expeditionRepository.findByStatus(StatusEnum.CREATE, pageable);
        }


        return expeditions.stream()
                .filter(exp -> exp.getStatus() != StatusEnum.DELETE)
                .sorted(Comparator.comparing(Expeditions::getCreatedAt).reversed())
                .map(exp -> {
                    ExpeditionDto dto = new ExpeditionDto();
                    dto.setId(exp.getId());
                    dto.setCbn(exp.getCbn());
                    dto.setRef(exp.getRef());
                    dto.setExpeditionType(exp.getExpeditionType());
                    dto.setWeight(exp.getWeight());
                    dto.setArrivalDate(exp.getArrivalDate());
                    dto.setStartDate(exp.getStartDate());
                    dto.setStartCountry(exp.getStartCountry());
                    dto.setDestinationCountry(exp.getDestinationCountry());
                    dto.setClientId(exp.getClient() != null ? exp.getClient().getId() : null);
                    dto.setClientName(exp.getClient() != null ? exp.getClient().getFirstName() + " " + exp.getClient().getLastName() : null);

                    return dto;
                })
                .collect(Collectors.toList());

    }
}
