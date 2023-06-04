package com.project.deliveryservice.domain.delivery.service;

import com.project.deliveryservice.common.entity.Address;
import com.project.deliveryservice.domain.delivery.entity.Delivery;
import com.project.deliveryservice.domain.delivery.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;

    public Delivery createDelivery(String city, String street, String zipCode) {
        Address address = new Address(city, street, zipCode);
        return Delivery.builder()
                .address(address)
                .build();
    }
}
