package com.weizhi.tesla.tracker;

import java.util.*;

public class TeslaInventoryTracker {

    public static void main(String[] args) {
        String condition = ApplicationProperties.INSTANCE.getCondition();
        int timeInterval = Integer.parseInt(ApplicationProperties.INSTANCE.getTimeInterval());

        String sid = ApplicationProperties.INSTANCE.getTwilioSid();
        String authToken = ApplicationProperties.INSTANCE.getTwilioAuthToken();
        String model = ApplicationProperties.INSTANCE.getModel();
        String phone = ApplicationProperties.INSTANCE.getPhone();
        Timer timer = new Timer();
        if (condition.equals("both")) {
            timer.schedule(new CheckTask(phone, sid, authToken, "new", model), 0, timeInterval * 1000L);
            timer.schedule(new CheckTask(phone, sid, authToken, "used", model), 0, timeInterval * 1000L);
        } else {
            timer.schedule(new CheckTask(phone, sid, authToken, condition, model), 0, timeInterval * 1000L);
        }
    }
}
