package com.example.demo.dto;

import com.example.demo.entity.Amenity;

public class AmenityDTO {
    private Long id;
    private String aname;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAname() {
        return aname;
    }

    public void setAname(String aname) {
        this.aname = aname;
    }

    public AmenityDTO() {}
    public AmenityDTO(Long id, String aname) {
        this.id = id;
        this.aname = aname;
    }

    public static AmenityDTO from(Amenity entity) {
        AmenityDTO dto = new AmenityDTO();
        dto.setId(entity.getId());
        dto.setAname(entity.getAname());
        return dto;
    }
}
