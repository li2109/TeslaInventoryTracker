package com.weizhi.tesla.tracker;

import lombok.Data;

import java.util.List;

@Data
public class TeslaAPIQueryObject {
  @Data
  public static class Query {
    String model;
    String condition;
    List<String> options;
    String arrangeby;
    String order;
    String market;
    String language;
    String super_region;
    double lng;
    double lat;
    String zip;
    int range;
    String region;
  }

  Query query;
  int offset;
  int count;
  int outsideOffset;
  boolean outsideSearch;
}
