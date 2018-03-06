package com.cognizant.devops.platformcommons.core.enums;

public enum AGENTSTATUS {

	START("RUNNING"),
	STOP("STOPPED");
	
    private String value;

    AGENTSTATUS(final String value) {
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
