package com.etransportation.enums;

public enum TimeKeepingStatus {
    IN_TIME("In Time"),
    lATE_IN("Late In"),
    ON_TIME("On Time"),
    EARLY_OUT("Early Out");

    private final String text;

    TimeKeepingStatus(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
