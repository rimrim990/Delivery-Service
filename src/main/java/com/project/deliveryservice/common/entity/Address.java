package com.project.deliveryservice.common.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    private String city;

    private String street;

    private String zipcode;

    @Override
    public String toString() {
        return  city + ' ' +
                street + ' ' +
                zipcode;
    }
}
