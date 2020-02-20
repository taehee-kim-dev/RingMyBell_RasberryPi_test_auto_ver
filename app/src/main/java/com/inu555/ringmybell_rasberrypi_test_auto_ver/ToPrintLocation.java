package com.inu555.ringmybell_rasberrypi_test_auto_ver;

public class ToPrintLocation {
    private String stopName;
    private String stopIdentifier;

    public ToPrintLocation(String stopName, String stopIdentifier) {
        this.stopName = stopName;
        this.stopIdentifier = stopIdentifier;
    }

    public String getStopName() {
        return stopName;
    }

    public String getStopIdentifier() {
        return stopIdentifier;
    }
}
