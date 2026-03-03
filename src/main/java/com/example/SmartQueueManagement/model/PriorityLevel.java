package com.example.SmartQueueManagement.model;

public enum PriorityLevel {
    NORMAL(10),
    ELDERLY(50),
    EMERGENCY(100);

    private final int baseWeight;

    PriorityLevel(int baseWeight) {
        this.baseWeight = baseWeight;
    }

    public int getBaseWeight() {
        return baseWeight;
    }
}
