package com.weizhi.tesla.tracker;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;


@JsonIgnoreProperties(ignoreUnknown = true)
public class OfficialTeslaInventory {

    @JsonProperty("VIN")
    private String vin;

    @JsonProperty("TrimName")
    private String trimName;

    @JsonProperty("TotalPrice")
    private Long totalPrice;

    @JsonProperty("InventoryPrice")
    private Long inventoryPrice;

    @JsonProperty("MonroneyPrice")
    private Long outTheDoorPrice;

    @JsonProperty("PAINT")
    private List<String> paint;

    @JsonProperty("WHEELS")
    private List<String> wheels;

    @JsonProperty("INTERIOR")
    private List<String> interior;

    @JsonProperty("DECOR")
    private List<String> decor;

    @JsonProperty("Year")
    private String year;

    @JsonProperty("City")
    private String city;

    @JsonProperty("StateProvince")
    private String state;

    @JsonProperty("TitleStatus")
    private String titleStatus;

    @JsonProperty("TRIM")
    private List<String> trim;

    @JsonProperty("CurrencyCode")
    private String currencyCode;

    @JsonProperty("CountryCode")
    private String countryCode;

    @JsonProperty("Model")
    private String model;

    @JsonProperty("Odometer")
    private String odometer;

    @JsonProperty("IsDemo")
    private String isDemo;

    @JsonProperty("OptionCodeData")
    private List<OptionCode> optionCodeData;

    @JsonProperty("ADL_OPTS")
    private List<String> additionalOptions;

    @JsonProperty("CABIN_CONFIG")
    private List<String> cabinConfig;

    @JsonProperty("AUTOPILOT")
    private List<String> autopilot;

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getTrimName() {
        return trimName;
    }

    public void setTrimName(String trimName) {
        this.trimName = trimName;
    }

    public String getTotalPrice() {
        return String.format("%s %s", Optional.ofNullable(totalPrice).orElse(inventoryPrice), currencyCode);
    }

    public long getInventoryPrice() {
        return inventoryPrice;
    }

    public void setInventoryPrice(long inventoryPrice) {
        this.inventoryPrice = inventoryPrice;
    }

    public void setTotalPrice(long totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getOutTheDoorPrice() {
        return String.format("%s %s", Optional.ofNullable(outTheDoorPrice).map(String::valueOf).orElse("<none>"), currencyCode);
    }

    public void setOutTheDoorPrice(long outTheDoorPrice) {
        this.outTheDoorPrice = outTheDoorPrice;
    }

    public List<String> getPaint() {
        return paint;
    }

    public void setPaint(List<String> paint) {
        this.paint = paint;
    }

    public List<String> getWheels() {
        return Optional.ofNullable(wheels).orElse(List.of(getOptionCode("WHEELS").getName()));
    }

    public void setWheels(List<String> wheels) {
        this.wheels = wheels;
    }

    public List<String> getInterior() {
        return interior;
    }

    public void setInterior(List<String> interior) {
        this.interior = interior;
    }

    public List<String> getDecor() {
        return decor;
    }

    public void setDecor(List<String> decor) {
        this.decor = decor;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getTitleStatus() {
        return titleStatus;
    }

    public void setTitleStatus(String titleStatus) {
        this.titleStatus = titleStatus;
    }

    public String getTrim() {
        return trim.get(0);
    }

    public void setTrim(List<String> trim) {
        this.trim = trim;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getOdometer() {
        return odometer;
    }

    public void setOdometer(String odometer) {
        this.odometer = odometer;
    }

    public String getIsDemo() {
        return isDemo;
    }

    public void setIsDemo(String isDemo) {
        this.isDemo = isDemo;
    }

    public List<String> getAdditionalOptions() {
        return Optional.ofNullable(additionalOptions).orElseGet(List::of);
    }

    public void setAdditionalOptions(List<String> additionalOptions) {
        this.additionalOptions = additionalOptions;
    }

    public List<String> getCabinConfig() {
        return cabinConfig;
    }

    public void setCabinConfig(List<String> cabinConfig) {
        this.cabinConfig = cabinConfig;
    }

    public List<OptionCode> getOptionCodeData() {
        return optionCodeData;
    }

    public void setOptionCodeData(List<OptionCode> optionCodeData) {
        this.optionCodeData = optionCodeData;
    }

    public List<String> getAutopilot() {
        return autopilot;
    }

    public void setAutopilot(List<String> autopilot) {
        this.autopilot = autopilot;
    }

    public OptionCode getOptionCode(final String groupKey) {
        return Optional.ofNullable(getOptionCodeData())
                .stream()
                .flatMap(Collection::stream)
                .filter(oc -> groupKey.equalsIgnoreCase(oc.getGroup()))
                .findFirst()
                .orElse(null);
    }

    public String getRange() {
        return Optional.ofNullable(getOptionCode("SPECS_RANGE"))
                .map(oc -> oc.getValue() + oc.getUnitShort())
                .orElse("");

    }

    public String getUrl() {
        if ("CA".equals(countryCode)) {
            return String.format("<https://www.tesla.com/en_CA/%s/order/%s#payment>", getModel(), getVin());
        } else {
            return String.format("<https://www.tesla.com/%s/order/%s#payment>", getModel(), getVin());
        }
    }

    public String getUrl(final String referral) {
        if (referral == null) {
            return getUrl();
        }

        if ("CA".equals(countryCode)) {
            return String.format("<https://www.tesla.com/en_CA/%s/order/%s?referral=%s#payment>", getModel(), getVin(), referral);
        } else {
            return String.format("<https://www.tesla.com/%s/order/%s?referral=%s#payment>", getModel(), getVin(), referral);
        }
    }

    public String getName() {
        return String.format("%s %s %s %s", getTitleStatus(), getYear(), getModel().toUpperCase(), getTrimName());
    }

    public String getLocation() {
        return String.format("%s, %s", getCity(), getState());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OfficialTeslaInventory that = (OfficialTeslaInventory) o;
        return Objects.equals(vin, that.vin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vin);
    }

    @Override
    public String toString() {
        return "com.weizhi.tesla.tracker.OfficialTeslaInventory{" +
                "vin='" + vin + '\'' +
                ", trimName='" + trimName + '\'' +
                ", totalPrice=" + totalPrice +
                ", inventoryPrice=" + inventoryPrice +
                ", outTheDoorPrice=" + outTheDoorPrice +
                ", paint=" + paint +
                ", wheels=" + wheels +
                ", interior=" + interior +
                ", decor=" + decor +
                ", year='" + year + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", titleStatus='" + titleStatus + '\'' +
                ", trim=" + trim +
                ", currencyCode='" + currencyCode + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", model='" + model + '\'' +
                ", odometer='" + odometer + '\'' +
                ", isDemo='" + isDemo + '\'' +
                ", optionCodeData=" + optionCodeData +
                ", additionalOptions=" + additionalOptions +
                ", cabinConfig=" + cabinConfig +
                ", autopilot=" + autopilot +
                '}';
    }
}