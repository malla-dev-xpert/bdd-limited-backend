package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.repository.DevisesRepository;
import com.xpertpro.bbd_project.repository.ExchangeRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        final Logger logger = LoggerFactory.getLogger(ExchangeRateServices.class);

        try {
            // Validation des paramètres
            if (fromCode == null || toCode == null || fromCode.length() != 3 || toCode.length() != 3) {
                throw new IllegalArgumentException("Codes devise invalides");
            }

            String url = String.format("https://open.er-api.com/v6/latest/%s", fromCode);
            logger.debug("Appel API taux de change: {}", url);

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                logger.error("Erreur API taux: {}", response.getStatusCode());
                throw new RuntimeException("Service de taux indisponible");
            }

            Map body = response.getBody();
            if (body == null || !"success".equals(body.get("result"))) {
                logger.error("Réponse API invalide: {}", body);
                throw new RuntimeException("Réponse API invalide");
            }

            Map<String, Object> rates = (Map<String, Object>) body.get("rates");
            if (rates == null) {
                throw new RuntimeException("Données de taux manquantes");
            }

            Object rateValue = rates.get(toCode.toUpperCase());
            if (rateValue == null) {
                logger.warn("Taux non trouvé pour {}->{}", fromCode, toCode);
                return null;
            }

            // Conversion robuste du taux
            if (rateValue instanceof Number) {
                return ((Number) rateValue).doubleValue();
            } else {
                logger.error("Type de taux invalide: {}", rateValue.getClass());
                throw new RuntimeException("Format de taux invalide");
            }

        } catch (RestClientException e) {
            logger.error("Erreur réseau avec l'API de taux", e);
            throw new RuntimeException("Service de taux indisponible", e);
        } catch (Exception e) {
            logger.error("Erreur inattendue", e);
            throw new RuntimeException("Erreur de traitement des taux", e);
        }
    }

//    public ExchangeRate saveExchangeRate(String fromCode, String toCode) {
//        try {
//            Double rate = getRealTimeRate(fromCode, toCode);
//            Devises from = devisesRepository.findByCode(fromCode)
//                    .orElseThrow(() -> new RuntimeException("Devise source '"+fromCode+"' introuvable"));
//            Devises to = devisesRepository.findByCode(toCode)
//                    .orElseThrow(() -> new RuntimeException("Devise cible '"+toCode+"' introuvable"));
//
//            ExchangeRate exchangeRate = new ExchangeRate();
//            exchangeRate.setFromDevise(from);
//            exchangeRate.setToDevise(to);
//            exchangeRate.setRate(rate);
//            exchangeRate.setTimestamp(LocalDateTime.now());
//            return exchangeRateRepository.save(exchangeRate);
//        } catch (Exception e) {
//            throw new RuntimeException("Erreur lors de la sauvegarde du taux de change: " + e.getMessage(), e);
//        }
//    }
}
