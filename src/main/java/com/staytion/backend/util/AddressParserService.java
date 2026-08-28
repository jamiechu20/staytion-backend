package com.staytion.backend.util;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.staytion.backend.entity.City;
import com.staytion.backend.entity.District;
import com.staytion.backend.repository.CityRepository;
import com.staytion.backend.repository.DistrictRepository;

@Service
@RequiredArgsConstructor
public class AddressParserService {

    private final CityRepository cityRepository;

    private final DistrictRepository districtRepository;

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

