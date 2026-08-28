package com.staytion.backend.dto.response;

public record LocationOption(String label, String value, String city, LocationType type) {
    public enum LocationType {CITY, DISTRICT}
}
