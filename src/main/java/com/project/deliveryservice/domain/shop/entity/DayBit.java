package com.project.deliveryservice.domain.shop.entity;

public enum DayBit {

    MON(1 << 1),
    TUE(1 << 2),
    WED(1 << 3),
    THU(1 << 4),
    FRI(1 << 5),
    SAT(1 << 6),
    SUN(1 << 7);

    private final int bit;

    DayBit(int bit) {
        this.bit = bit;
    }
}
