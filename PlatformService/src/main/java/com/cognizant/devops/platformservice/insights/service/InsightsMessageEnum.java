package com.cognizant.devops.platformservice.insights.service;

public enum InsightsMessageEnum {

	CURRENTVALZERO("01"),
	PREVIOUSVALZERO("10"),
	NEUTRALVALZERO("00"),
	ZEROVALMSG("zeroResult");
	
    private String value;

    InsightsMessageEnum(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.getValue();
    }
}
