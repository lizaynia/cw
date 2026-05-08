package com.common.dto;

import java.io.Serializable;
import java.util.Objects;

public class CityDto implements Serializable {
    private Integer id;
    private String cityName;

    public CityDto() {}

    public CityDto(Integer id, String cityName) {
        this.id = id;
        this.cityName = cityName;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }

    @Override
    public String toString() {
        return cityName != null ? cityName : "";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CityDto cityDto = (CityDto) obj;
        return Objects.equals(cityName, cityDto.cityName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cityName);
    }
}