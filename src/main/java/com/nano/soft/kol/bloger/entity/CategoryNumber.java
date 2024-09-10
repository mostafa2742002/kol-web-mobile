package com.nano.soft.kol.bloger.entity;

public record CategoryNumber(String name, int number, String image) {
    public CategoryNumber {
        if (number < 0) {
            throw new IllegalArgumentException("number should be positive");
        }
    }
}
