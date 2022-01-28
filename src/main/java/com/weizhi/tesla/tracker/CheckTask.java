package com.weizhi.tesla.tracker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.tuple.Pair;
import redis.clients.jedis.Jedis;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class CheckTask extends TimerTask {
    private static final Gson gson = new Gson();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final Jedis jedis = new Jedis("localhost");
    private String condition = "new";
    private String model = "my";
    private String phone = null;

    public CheckTask(String phone, String sid, String authToken, String condition, String model) {
        this.condition = condition;
        this.model = model;
        this.phone = phone;
        Twilio.init(sid, authToken);
    }

    @Override
    public void run() {
        Pair modelAndCondition = Pair.of(model, condition.equals("new"));
        TeslaAPIQueryObject teslaAPIQueryObject = buildQueryObject(modelAndCondition);

        String jsonQueryParam = gson.toJson(teslaAPIQueryObject);
        String resp =
                sendHttpRequest(
                        "https://www.tesla.com/inventory/api/v1/inventory-results?query=" + jsonQueryParam);
        if (resp != null) {
            analyzeResponse(modelAndCondition, resp);
        }
    }

    private void analyzeResponse(Pair<String, Boolean> modelAndConditionPair, String resp) {
        TeslaApiNoInventoryResponse teslaApiNoInventoryResponse = null;
        TeslaApiHasInventoryResponse teslaApiHasInventoryResponse = null;

        try {
            teslaApiNoInventoryResponse = mapper.readValue(resp, TeslaApiNoInventoryResponse.class);
            teslaApiHasInventoryResponse = mapper.readValue(resp, TeslaApiHasInventoryResponse.class);
        } catch (JsonProcessingException e) {
        }
        if (teslaApiHasInventoryResponse != null
                && teslaApiHasInventoryResponse.getResults() != null
                && teslaApiHasInventoryResponse.getTotalMatchesFound() != null) {
            int totalMatchesFound = Integer.parseInt(teslaApiHasInventoryResponse.getTotalMatchesFound());
            if (totalMatchesFound > 0) {
                parseResultAndSendSms(modelAndConditionPair, teslaApiHasInventoryResponse.getResults());
                return;
            }
        } else if (teslaApiNoInventoryResponse != null
                && teslaApiNoInventoryResponse.getResults() != null
                && teslaApiNoInventoryResponse.getResults().approximate != null
                && teslaApiNoInventoryResponse.getResults().approximate != null
                && teslaApiNoInventoryResponse.getResults().approximateOutside != null
                && teslaApiNoInventoryResponse.getTotalMatchesFound() != null) {
            int totalSplitMatchesFound =
                    Integer.parseInt(teslaApiNoInventoryResponse.getTotalMatchesFound());
            if (totalSplitMatchesFound > 0) {
                List<OfficialTeslaInventory> inventories = List.of();
                if (!teslaApiNoInventoryResponse.getResults().exact.isEmpty()) {
                    inventories.addAll(teslaApiNoInventoryResponse.getResults().exact);
                }
                if (!teslaApiNoInventoryResponse.getResults().approximate.isEmpty()) {
                    inventories.addAll(teslaApiNoInventoryResponse.getResults().approximate);
                }
                if (!teslaApiNoInventoryResponse.getResults().approximateOutside.isEmpty()) {
                    inventories.addAll(teslaApiNoInventoryResponse.getResults().approximateOutside);
                }
                parseResultAndSendSms(modelAndConditionPair, inventories);
                return;
            }
        }
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String strDate = dateFormat.format(date);
        System.out.printf(
                "No Inventory: -- %s %s at %s\n",
                modelAndConditionPair.getRight() ? "New" : "Used",
                modelAndConditionPair.getLeft(),
                strDate);
    }

    private void parseResultAndSendSms(
            Pair<String, Boolean> modelAndConditionPair, List<OfficialTeslaInventory> inventories) {
        List<OfficialTeslaInventory> availableInventories =
                inventories.stream()
                        .filter(inventory -> !jedis.exists(inventory.getVin()))
                        .filter(inventory -> checkIfAvailable(inventory.getUrl()))
                        .collect(Collectors.toList());

        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String strDate = dateFormat.format(date);
        System.out.printf(
                "Inventory Found: %d %s %s at %s\n",
                availableInventories.size(),
                modelAndConditionPair.getRight() ? "New" : "Used",
                modelAndConditionPair.getLeft(),
                strDate);

        for (int i = 0; i < availableInventories.size(); i++) {
            OfficialTeslaInventory inventory = availableInventories.get(i);
            String msg = "";
            if (i == 0) {
                msg += String.format("%d %s Model Y Found!\n", availableInventories.size(), this.condition);
            }
            msg += String.format("Index: %d\n", i);
            msg += String.format("Year: %s\n", inventory.getYear());
            msg += String.format("Paint: %s\n", inventory.getPaint().toString());
            msg += String.format("Out the door Price: %s\n", inventory.getOutTheDoorPrice());
            msg += String.format("Url: %s\n", inventory.getUrl());
            msg += String.format("Odometer: %s\n", inventory.getOdometer());
            msg += String.format("Location: %s\n", inventory.getLocation());
            msg += String.format("VIN: %s\n", inventory.getVin());
            msg += String.format("Wheels: %s\n", inventory.getWheels().toString());

            if (phone != null) {
                notifyUser(phone, msg);
            }

            jedis.set(inventory.getVin(), String.valueOf(true));
            jedis.expire(inventory.getVin(), 60 * 60 * 10);
        }
    }

    private static void notifyUser(String phone, String msg) {
        Message message =
                Message.creator(
                                new com.twilio.type.PhoneNumber(phone), "MG64d629689145bf8e7702fe502b3b3ddd", msg)
                        .create();
    }

    private static TeslaAPIQueryObject buildQueryObject(Pair<String, Boolean> modelAndConditionPair) {
        TeslaAPIQueryObject teslaAPIQueryObject = new TeslaAPIQueryObject();
        teslaAPIQueryObject.setQuery(buildQuery(modelAndConditionPair));
        teslaAPIQueryObject.setOffset(0);
        teslaAPIQueryObject.setCount(50);
        teslaAPIQueryObject.setOutsideOffset(0);
        teslaAPIQueryObject.setOutsideSearch(false);
        return teslaAPIQueryObject;
    }

    private static TeslaAPIQueryObject.Query buildQuery(Pair<String, Boolean> modelAndConditionPair) {
        TeslaAPIQueryObject.Query query = new TeslaAPIQueryObject.Query();
        query.setModel(modelAndConditionPair.getLeft());
        if (modelAndConditionPair.getRight()) {
            query.setCondition("new");
        } else {
            query.setCondition("used");
        }
        query.setOptions(new ArrayList<String>());
        query.setArrangeby("Price");
        query.setOrder("asc");
        query.setMarket("US");
        query.setLanguage("en");
        query.setSuper_region("north america");
        query.setLng(-121.9343805);
        query.setLat(37.4038124);
        query.setZip("95134");
        query.setRange(200);
        query.setRegion("CA");
        return query;
    }

    private static String sendHttpRequest(String url) {
        try {
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean checkIfAvailable(String url) {
        url = url.replace("<", "").replace(">", "");
        Response response;
        try {
            Request request = new Request.Builder().url(url).build();
            response = client.newCall(request).execute();
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
        if (response == null) return true;
        return response.code() != 404;
    }
}
