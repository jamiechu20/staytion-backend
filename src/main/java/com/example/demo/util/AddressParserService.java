package com.example.demo.util;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.City;
import com.example.demo.entity.District;
import com.example.demo.repository.CityRepository;
import com.example.demo.repository.DistrictRepository;

@Service
public class AddressParserService {

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private DistrictRepository districtRepository;

    public District parseDistrictFromAddress(String fullAddress) {
        List<City> cities = cityRepository.findAll();

        for (City city : cities) {
            if (fullAddress.startsWith(city.getCname())) {
                List<District> districts = districtRepository.findByCityId(city.getId());
                for (District district : districts) {
                    if (fullAddress.contains(district.getDname())) {
                        return district;
                    }
                }
            }
        }

        throw new RuntimeException("無法從地址中解析出有效的區域資訊");
    }
}

