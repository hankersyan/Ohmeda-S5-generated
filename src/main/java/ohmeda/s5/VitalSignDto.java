package com.ohmeda.s5;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VitalSignDto {
    private String adapterName;
    private String bedNo;
    private Date dateTime;
    private String wardNo;
    private String patientId;
    private List<VitalSignItemDto> items;

    public VitalSignDto() {
        items = new ArrayList<>();
    }

    public String getAdapterName() {
        return adapterName;
    }

    public void setAdapterName(String adapterName) {
        this.adapterName = adapterName;
    }

    public String getBedNo() {
        return bedNo;
    }

    public void setBedNo(String bedNo) {
        this.bedNo = bedNo;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public String getWardNo() {
        return wardNo;
    }

    public void setWardNo(String wardNo) {
        this.wardNo = wardNo;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public List<VitalSignItemDto> getItems() {
        return items;
    }

    public void setItems(List<VitalSignItemDto> items) {
        this.items = items;
    }

    public void add(String name, String value, String unit, ConfigurationItemLevel level) {
        items.add(new VitalSignItemDto(name, value, unit, level));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (VitalSignItemDto itm : items) {
            sb.append(String.format("%s,%s;", itm.getItem(), itm.getValue()));
        }
        return dateTime.toString() + ", " + sb.toString();
    }
}

class VitalSignItemDto {
    private String item;
    private String value;
    private String unit;
    private ConfigurationItemLevel level;

    public VitalSignItemDto(String item, String value, String unit, ConfigurationItemLevel level) {
        this.item = item;
        this.value = value;
        this.unit = unit;
        this.level = level;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public ConfigurationItemLevel getLevel() {
        return level;
    }

    public void setLevel(ConfigurationItemLevel level) {
        this.level = level;
    }
}

enum ConfigurationItemLevel {
    Important,
    None
}