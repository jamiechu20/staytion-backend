package com.example.demo.service;

import com.example.demo.dto.response.LocationOption;
import com.example.demo.entity.City;
import com.example.demo.entity.District;
import com.example.demo.repository.CityRepository;
import com.example.demo.repository.DistrictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;

    public List<LocationOption>  getAllCities() {
        List<City> cities = cityRepository.findAll();
        return cities.stream().map(city -> {
            String label = city.getCname();
            String value = city.getCname();
            return new LocationOption(label, value, null, null);
        }).toList();
    }
    public List<LocationOption> getDistrictsByCity(String cityName) {
        List<District> districts = districtRepository.findByCity_Cname(cityName);
        return districts.stream().map(dist ->{
            String label = dist.getDname();
            String value = dist.getDname();
            return new LocationOption(label, value, null, null);
        }).toList();
    }
    public List<LocationOption> getAllDistricts() {
        List<District> districts = districtRepository.findAll();
        return districts.stream().map(dist -> {
            String label = dist.getDname();
            String value = dist.getDname();
            String city = dist.getCity().getCname();
            return new LocationOption(label, value, city, null);
        }).toList();
    }
    public List<LocationOption> searchLocations(String keyword) {
        List<City> cities = cityRepository.findByCnameContainingIgnoreCase(keyword);
        List<District> districts = districtRepository.findByDnameContainingIgnoreCase(keyword);
        Stream<LocationOption> cityOptions = cities.stream().map(city -> {
            String label = city.getCname();
            String value = city.getCname();
            return new LocationOption(label, value, null, LocationOption.LocationType.CITY);
        });
        Stream<LocationOption> districtOptions = districts.stream().map(district -> {
            String label = district.getDname();
            String city = district.getCity().getCname();
            String value = district.getDname();
            return new LocationOption(label + "（" + city + "）", value, city, LocationOption.LocationType.DISTRICT);
        });
        return Stream.concat(cityOptions, districtOptions).toList();
    }

}
