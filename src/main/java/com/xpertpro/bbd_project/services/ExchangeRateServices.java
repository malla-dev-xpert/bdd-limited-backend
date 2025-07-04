package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.entity.Devises;
import com.xpertpro.bbd_project.entity.ExchangeRate;
import com.xpertpro.bbd_project.repository.DevisesRepository;
import com.xpertpro.bbd_project.repository.ExchangeRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class ExchangeRateServices {
    @Autowired
    ExchangeRateRepository exchangeRateRepository;
    @Autowired
    DevisesRepository devisesRepository;

    private final RestTemplate restTemplate;
    public ExchangeRateServices(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Double getRealTimeRate(String fromCode, String toCode) {
        String url = String.format("https://open.er-api.com/v6/latest/%s", fromCode);
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map body = response.getBody();
            if ("success".equals(body.get("result"))) {
                Map<String, Object> rates = (Map<String, Object>) body.get("rates");
                Object rateValue = rates.get(toCode);

                // Gérer à la fois les cas entiers et doubles
                if (rateValue instanceof Integer) {
                    return ((Integer) rateValue).doubleValue();
                } else if (rateValue instanceof Double) {
                    return (Double) rateValue;
                } else {
                    throw new RuntimeException("Type de taux invalide renvoyé depuis l’API");
                }
            }
        }
        throw new RuntimeException("Échec de récupération du taux de change.");
    }

    public ExchangeRate saveExchangeRate(String fromCode, String toCode) {
        try {
            Double rate = getRealTimeRate(fromCode, toCode);
            Devises from = devisesRepository.findByCode(fromCode)
                    .orElseThrow(() -> new RuntimeException("Devise source '"+fromCode+"' introuvable"));
            Devises to = devisesRepository.findByCode(toCode)
                    .orElseThrow(() -> new RuntimeException("Devise cible '"+toCode+"' introuvable"));

            ExchangeRate exchangeRate = new ExchangeRate();
            exchangeRate.setFromDevise(from);
            exchangeRate.setToDevise(to);
            exchangeRate.setRate(rate);
            exchangeRate.setTimestamp(LocalDateTime.now());
            return exchangeRateRepository.save(exchangeRate);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la sauvegarde du taux de change: " + e.getMessage(), e);
        }
    }
}
