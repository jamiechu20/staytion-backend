package com.example.demo.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HotelJsonDto {

	@JsonProperty("HotelName")
    private String hotelName;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("PositionLat")
    private Double positionLat;

    @JsonProperty("PositionLon")
    private Double positionLon;

    @JsonProperty("Telephones")
    private List<Telephone> telephones;

    @JsonProperty("PostalAddress")
    private PostalAddress postalAddress;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Telephone {
    	@JsonProperty("Tel")
        private String tel;

        public String getTel() {
            return tel;
        }

        public void setTel(String tel) {
            this.tel = tel;
        }
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PostalAddress {
    	@JsonProperty("City")
        private String city;

        @JsonProperty("Town")
        private String town;

        @JsonProperty("StreetAddress")
        private String streetAddress;


        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getTown() {
            return town;
        }

        public void setTown(String town) {
            this.town = town;
        }

        public String getStreetAddress() {
            return streetAddress;
        }

        public void setStreetAddress(String streetAddress) {
            this.streetAddress = streetAddress;
        }
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPositionLat() {
        return positionLat;
    }

    public void setPositionLat(Double positionLat) {
        this.positionLat = positionLat;
    }

    public Double getPositionLon() {
        return positionLon;
    }

    public void setPositionLon(Double positionLon) {
        this.positionLon = positionLon;
    }

    public List<Telephone> getTelephones() {
        return telephones;
    }

    public void setTelephones(List<Telephone> telephones) {
        this.telephones = telephones;
    }

    public PostalAddress getPostalAddress() {
        return postalAddress;
    }

    public void setPostalAddress(PostalAddress postalAddress) {
        this.postalAddress = postalAddress;
    }
    
    
}
