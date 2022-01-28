package com.weizhi.tesla.tracker;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TeslaApiNoInventoryResponse {

  @JsonProperty("results")
  private Results results;

  @JsonProperty("total_matches_found")
  private String totalMatchesFound;

  public Results getResults() {
    return results;
  }

  public void setResults(Results results) {
    this.results = results;
  }

  public String getTotalMatchesFound() {
    return totalMatchesFound;
  }

  public void setTotalMatchesFound(String totalMatchesFound) {
    this.totalMatchesFound = totalMatchesFound;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public class Results {
    @JsonProperty("exact")
    List<OfficialTeslaInventory> exact;

    @JsonProperty("approximate")
    List<OfficialTeslaInventory> approximate;

    @JsonProperty("approximateOutside")
    List<OfficialTeslaInventory> approximateOutside;
  }
}
