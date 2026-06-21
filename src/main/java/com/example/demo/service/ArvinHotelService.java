package com.example.demo.service;

import java.util.List;
import java.util.stream.Collectors;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.ImageDTO;
import com.example.demo.dto.request.HotelRequestDto;
import com.example.demo.dto.response.HotelResponseDto;
import com.example.demo.util.AddressParserService;
import com.example.demo.util.GeocodingService;

@Service
public class ArvinHotelService {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DistrictRepository districtRepository;
    
    @Autowired
    private ImageRepository imageRepository;
    
    @Autowired
    private AddressParserService addressParserService;
    
    @Autowired
    private GeocodingService geocodingService;

    @Autowired
    private AmenityRepository amenityRepository;

//    查詢全部飯店(但應該不需要?)
//    public List<HotelResponseDto> getAllHotels() {
//        return hotelRepository.findAll()
//                .stream()
//                .map(this::toDto)
//                .collect(Collectors.toList());
//    }
    
    // 查詢指定使用者的飯店
    public List<HotelResponseDto> getHotelsByOwner(Long ownerId) {
        return hotelRepository.findByOwnerId(ownerId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public HotelResponseDto getHotelById(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));
        return toDto(hotel);
    }

    public HotelResponseDto saveHotel(HotelRequestDto dto) {
        Hotel hotel = new Hotel();
        return saveOrUpdate(hotel, dto);
    }

    @Transactional
    public HotelResponseDto updateHotel(Long id, HotelRequestDto dto) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));
        return saveOrUpdate(hotel, dto);
    }

    public void deleteHotel(Long id) {
        hotelRepository.deleteById(id);
    }

    @Transactional
    private HotelResponseDto saveOrUpdate(Hotel hotel, HotelRequestDto dto) {
        hotel.setHname(dto.getHotelname());
        hotel.setAddress(dto.getAddress());
        hotel.setTel(dto.getTel());
        hotel.setDescription(dto.getDescription());
        
        // 自動解析地址 → District
        District district = addressParserService.parseDistrictFromAddress(dto.getAddress());
        hotel.setDistrict(district);
       
        // 自動轉換地址 → 經緯度
        double[] latLng = geocodingService.getLatLng(dto.getAddress());
        hotel.setLatitude(latLng[0]);
        hotel.setLongitude(latLng[1]);

        if (dto.getOwnerId() != null) {
            User owner = userRepository.findById(dto.getOwnerId())
                    .orElseThrow(() -> new RuntimeException("Owner not found"));
            hotel.setOwner(owner);
        }
        
        hotel = hotelRepository.save(hotel); // 先儲存 hotel 以便關聯
        final Hotel savedHotel = hotel;

        imageRepository.deleteByHotel(hotel);

        hotel.getImages().clear();

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            List<Image> newImages = dto.getImages().stream()
                .map(imgDto -> {
                    Image img = new Image();
                    img.setHotel(savedHotel);
                    img.setImgUrl(imgDto.getImgUrl());
                    img.setIsCover(imgDto.getIsCover() != null && imgDto.getIsCover());
                    return img;
                })
                .collect(Collectors.toList());
            hotel.getImages().addAll(newImages);
        }

        if (dto.getAmenities() != null) {
            hotel.getAmenities().clear();
            List<Amenity> amenities = amenityRepository.findAllById(dto.getAmenities());
            hotel.getAmenities().addAll(amenities);
        }

        // 再存一次 hotel，JPA 會 cascade persist 新的圖片
        hotel = hotelRepository.save(hotel);

//        return toDto(hotelRepository.save(hotel));
        return toDto(hotel);
    }

    private HotelResponseDto toDto(Hotel hotel) {
        HotelResponseDto dto = new HotelResponseDto();
        dto.setId(hotel.getId());
        dto.setHotelname(hotel.getHname());
        dto.setAddress(hotel.getAddress());
        dto.setTel(hotel.getTel());
        dto.setDescription(hotel.getDescription());
        dto.setLatitude(hotel.getLatitude());
        dto.setLongitude(hotel.getLongitude());

        if (hotel.getOwner() != null) {
            dto.setOwnerId(hotel.getOwner().getId());
            dto.setOwnerName(hotel.getOwner().getFirstName() + " " + hotel.getOwner().getLastName());
        }

        if (hotel.getDistrict() != null) {
            dto.setDistrictId(hotel.getDistrict().getId());
            dto.setDistrictName(hotel.getDistrict().getDname());
        }

        if (hotel.getImages() != null && !hotel.getImages().isEmpty()) {
            List<ImageDTO> imageDtos = hotel.getImages().stream()
                    .map(img -> {
                        ImageDTO i = new ImageDTO();
                        i.setId(img.getId());
                        if (img.getHotel() != null) {
                            i.setHotelId(img.getHotel().getId());
                        }
                        i.setImgUrl(img.getImgUrl());
                        i.setIsCover(img.getIsCover());
                        return i;
                    })
                    .collect(Collectors.toList());
            dto.setImages(imageDtos);

            // 額外處理：轉為 imgUrls (List<String>) 及主圖 mainImgUrl
            List<String> imgUrls = imageDtos.stream()
                    .map(ImageDTO::getImgUrl)
                    .collect(Collectors.toList());
            dto.setImgUrls(imgUrls);

            imageDtos.stream()
                    .filter(ImageDTO::getIsCover)
                    .findFirst()
                    .ifPresent(mainImg -> dto.setMainImgUrl(mainImg.getImgUrl()));
        }

        if (hotel.getAmenities() != null) {
            List<Long> ids = hotel.getAmenities().stream().map(Amenity::getId).collect(Collectors.toList());
            dto.setAmenityIds(ids);
        }

        
        return dto;
    }
}
