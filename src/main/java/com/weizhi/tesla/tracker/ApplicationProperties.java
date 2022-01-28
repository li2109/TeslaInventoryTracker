package com.weizhi.tesla.tracker;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum ApplicationProperties {
    INSTANCE;

    private final Properties properties;

    ApplicationProperties() {
        properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("application.properties"));
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public String getTwilioSid() {
        return properties.getProperty("twilio.sid");
    }

    public String getTwilioAuthToken() {
        return properties.getProperty("twilio.authToken");
    }

    public String getModel() {
        return properties.getProperty("tesla.model");
    }

    public String getTimeInterval() {
        return properties.getProperty("tracker.timer");
    }

    public String getCondition() {
        return properties.getProperty("tracker.condition");
    }

    public String getPhone() {
        return properties.getProperty("tracker.phone");
    }
}
