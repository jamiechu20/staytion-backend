package com.staytion.backend.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;

import com.staytion.backend.projection.HotelProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.staytion.backend.dto.AmenityDTO;
import com.staytion.backend.dto.HotelDetailDTO;
import com.staytion.backend.dto.HotelSearchRequest;
import com.staytion.backend.dto.HotelSearchResult;
import com.staytion.backend.dto.ImageDTO;
import com.staytion.backend.dto.RoomTypeDTO;
import com.staytion.backend.entity.Hotel;
import com.staytion.backend.repository.HotelRepository;
import com.staytion.backend.repository.ImageRepository;
import com.staytion.backend.repository.RoomTypeRepository;
import com.staytion.backend.specification.HotelSpecifications;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final ImageRepository imageRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<HotelSearchRequest> searchHotels(HotelSearchResult dto, int page, int size) {
        // 篩選、排序、分頁全部在資料庫完成，只載入當頁的飯店
        Specification<Hotel> spec = buildSearchSpec(dto);
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size);
        Page<Hotel> hotels = hotelRepository.findAll(spec, pageable);

        return hotels.map(hotel -> {
            HotelSearchRequest out = new HotelSearchRequest();
            out.setId(hotel.getId());
            out.setName(hotel.getHname());
            out.setCity(hotel.getDistrict().getCity().getCname());
            out.setDistrict(hotel.getDistrict().getDname());
            out.setLat(hotel.getLatitude());
            out.setLng(hotel.getLongitude());
            out.setImgUrl(
                    hotel.getImages().stream()
                            .filter(img -> Boolean.TRUE.equals(img.getIsCover()))
                            .map(img -> img.getImgUrl())
                            .findFirst()
                            .orElse("https://fakeimg.pl/200x200/?text=No+Image"));
                    // 建議編碼，避免有空格等字元
            String mapQuery = hotel.getHname() + " " + hotel.getDistrict().getCity().getCname();
            String mapUrl = "https://www.google.com/maps/search/?api=1&query=" +
                    URLEncoder.encode(mapQuery, StandardCharsets.UTF_8);
            out.setMapUrl(mapUrl);
            out.setRoomType(
                    hotel.getRoomTypes().stream()
                            .map(rt -> rt.getRname())
                            .findFirst()
                            .orElse(""));
            out.setPrice(
                    hotel.getRoomTypes().stream()
                            .map(rt -> rt.getPrice().intValue())
                            .min(Integer::compareTo)
                            .orElse(0));
            out.setRating(
                    hotel.getReviews().stream()
                            .mapToDouble(r -> r.getScore())
                            .average().orElse(0.0));
            out.setNight(getNight(dto.getCheckin(), dto.getCheckout()));
            out.setAdults(dto.getAdult() != null ? dto.getAdult() : 2);

            // 取得市中心經緯度
            double[] cityCenter = CITY_CENTER_MAP.get(hotel.getDistrict().getCity().getCname());
            if (cityCenter != null && hotel.getLatitude() != null && hotel.getLongitude() != null) {
                double distance = haversine(
                        hotel.getLatitude(), hotel.getLongitude(),
                        cityCenter[0], cityCenter[1]
                );
                out.setDistance(Math.round(distance * 10.0) / 10.0); // 單位: 公里
            } else {
                out.setDistance(null); // 查無資料時
            }

            return out;
        });
    }

    @Override
    public long countHotels(HotelSearchResult dto) {
        return hotelRepository.count(buildSearchSpec(dto));
    }

    private Specification<Hotel> buildSearchSpec(HotelSearchResult dto) {
        Integer totalPeople = null;
        if (dto.getAdult() != null || dto.getChild() != null) {
            totalPeople = (dto.getAdult() != null ? dto.getAdult() : 0)
                    + (dto.getChild() != null ? dto.getChild() : 0);
        }
        return Specification.allOf(
                HotelSpecifications.hasCapacityFor(totalPeople),
                HotelSpecifications.hasCity(dto.getCity()),
                HotelSpecifications.hasArea(dto.getArea()),
                HotelSpecifications.hasPriceInRange(dto.getPrice()),
                HotelSpecifications.hasAmenities(dto.getAmenity()),
                HotelSpecifications.hasMinScore(dto.getScore()),
                HotelSpecifications.sortBy(dto.getSort())
                );
    }

    // 飯店詳情
    @Override
    @Transactional(readOnly = true)
    public HotelDetailDTO getHotelDetail(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow();
        HotelDetailDTO dto = new HotelDetailDTO();
        dto.setId(hotel.getId());
        dto.setHname(hotel.getHname());
        dto.setAddress(hotel.getAddress());
        dto.setDescription(hotel.getDescription());

        // 飯店評分
        Double score = hotel.getReviews() != null && !hotel.getReviews().isEmpty()
                ? hotel.getReviews().stream().mapToDouble(r -> r.getScore()).average().orElse(0.0)
                : null;
        dto.setScore(score);

        // 查飯店圖片
        List<ImageDTO> imageDTOs = imageRepository.findByHotelIdAndIsCover(hotelId, false)
                .stream().map(img -> {
                    ImageDTO imgDTO = new ImageDTO();
                    imgDTO.setId(img.getId());
                    imgDTO.setImgUrl(img.getImgUrl());
                    imgDTO.setIsCover(img.getIsCover());
                    return imgDTO;
                }).collect(Collectors.toList());
        dto.setImages(imageDTOs);

        // 房型圖片：查詢條件跟房型無關，迴圈外查一次即可（原本每個房型都重查同一批資料）
        List<ImageDTO> roomImages = imageRepository.findByHotelIdAndIsCover(hotelId, true)
                .stream().map(img -> {
                    ImageDTO imgDTO = new ImageDTO();
                    imgDTO.setId(img.getId());
                    imgDTO.setImgUrl(img.getImgUrl());
                    imgDTO.setIsCover(img.getIsCover());
                    return imgDTO;
                }).collect(Collectors.toList());

        // 查所有房型（連同設施一起載入）
        List<RoomTypeDTO> roomTypeDTOs = roomTypeRepository.findWithAmenitiesByHotelId(hotelId)
                .stream().map(roomType -> {
                    RoomTypeDTO rtdto = new RoomTypeDTO();
                    rtdto.setId(roomType.getId());
                    rtdto.setRname(roomType.getRname());
                    rtdto.setDescription(roomType.getDescription());
                    rtdto.setPrice(roomType.getPrice());
                    rtdto.setSize(roomType.getSize());
                    rtdto.setView(roomType.getView());
                    rtdto.setImgUrl(roomType.getImgUrl());
                    rtdto.setIsCanceled(roomType.getIsCanceled());
                    rtdto.setQuantity(roomType.getQuantity());
                    rtdto.setBedCount(roomType.getBedCount());
                    rtdto.setBedType(roomType.getBedType());
                    rtdto.setCapacity(roomType.getCapacity());

                    rtdto.setImages(roomImages);

                    // 房型設施
                    List<AmenityDTO> rtAmenities = roomType.getAmenities()
                            .stream().map(a -> {
                                AmenityDTO adto = new AmenityDTO();
                                adto.setId(a.getId());
                                adto.setAname(a.getAname());
                                return adto;
                            }).collect(Collectors.toList());
                    rtdto.setAmenities(rtAmenities);

                    return rtdto;
                }).collect(Collectors.toList());
        dto.setRoomTypes(roomTypeDTOs);

        return dto;
    }

    @Override
    public List<HotelProjection> getTopHotels() {
        return hotelRepository.findTopHotels();
    }

	@Override
	@Transactional(readOnly = true)
	public List<HotelSearchRequest> searchHotelsByName(String keyword, Long highlightHotelId) {
		List<Hotel> hotels = hotelRepository.findByHnameContainingIgnoreCase(keyword);

		if (highlightHotelId != null) {
			Optional<Hotel> highlight = hotelRepository.findById(highlightHotelId);
			if (highlight.isPresent()) {
				Hotel h = highlight.get();
				if (h.getHname().toLowerCase().contains(keyword.toLowerCase())) {
					hotels.removeIf(hotel -> hotel.getId().equals(highlightHotelId));
					hotels.add(0, h);
				}
			}
		}

		return hotels.stream().map(hotel -> {
			HotelSearchRequest dto = new HotelSearchRequest();
			dto.setId(hotel.getId());
			dto.setName(hotel.getHname());

			// 城市與區域資訊
			dto.setDistrict(hotel.getDistrict().getDname());
			dto.setCity(hotel.getDistrict().getCity().getCname());

			// 位置座標與地圖連結
			dto.setLat(hotel.getLatitude());
			dto.setLng(hotel.getLongitude());
			dto.setMapUrl("https://maps.google.com/?q=" + hotel.getLatitude() + "," + hotel.getLongitude());

			// 房型資訊
			dto.setRoomType(hotel.getRoomTypes().stream().map(rt -> rt.getRname()).findFirst().orElse(""));

			// 價格最低房型
			dto.setPrice(hotel.getRoomTypes().stream().map(rt -> rt.getPrice().intValue()).min(Integer::compareTo)
					.orElse(0));

			// 評分平均
			double avgScore = hotel.getReviews().stream().mapToDouble(r -> r.getScore()).average().orElse(0.0);
			dto.setRating(avgScore);

			// 圖片封面
			dto.setImgUrl(hotel.getImages().stream().filter(img -> Boolean.TRUE.equals(img.getIsCover()))
					.map(img -> img.getImgUrl()).findFirst().orElse("https://fakeimg.pl/200x200/?text=No+Image"));

			// 其他欄位（如果你想在模糊搜尋階段就傳）
			dto.setAdults(2); // 預設值，如果你有查詢條件可以再處理
			dto.setNight(1); // 預設值

			return dto;
		}).collect(Collectors.toList());
	}

    private static final Map<String, double[]> CITY_CENTER_MAP = Map.ofEntries(
            Map.entry("台北市", new double[]{25.0478, 121.5170}),      // 台北車站
            Map.entry("新北市", new double[]{25.0123, 121.4637}),      // 板橋車站
            Map.entry("桃園市", new double[]{25.0120, 121.2152}),      // 桃園車站
            Map.entry("台中市", new double[]{24.1369, 120.6847}),      // 台中車站
            Map.entry("台南市", new double[]{22.9971, 120.2127}),      // 台南車站
            Map.entry("高雄市", new double[]{22.6408, 120.3020}),      // 高雄車站
            Map.entry("基隆市", new double[]{25.1319, 121.7394}),      // 基隆車站
            Map.entry("新竹市", new double[]{24.8016, 120.9717}),      // 新竹車站
            Map.entry("嘉義市", new double[]{23.4801, 120.4491}),      // 嘉義車站

            Map.entry("新竹縣", new double[]{24.8387, 121.0130}),      // 竹北火車站
            Map.entry("苗栗縣", new double[]{24.5646, 120.8235}),      // 苗栗車站
            Map.entry("彰化縣", new double[]{24.0685, 120.5417}),      // 彰化車站
            Map.entry("南投縣", new double[]{23.9130, 120.6848}),      // 南投縣政府
            Map.entry("雲林縣", new double[]{23.7092, 120.5422}),      // 斗六車站
            Map.entry("嘉義縣", new double[]{23.4811, 120.4491}),      // 嘉義車站（同嘉義市）
            Map.entry("屏東縣", new double[]{22.6727, 120.4852}),      // 屏東車站
            Map.entry("宜蘭縣", new double[]{24.7554, 121.7531}),      // 宜蘭車站
            Map.entry("花蓮縣", new double[]{23.9937, 121.6015}),      // 花蓮車站
            Map.entry("台東縣", new double[]{22.7964, 121.0703}),      // 台東車站

            Map.entry("澎湖縣", new double[]{23.5697, 119.5666}),      // 馬公市區
            Map.entry("金門縣", new double[]{24.4321, 118.3171}),      // 金城鎮
            Map.entry("連江縣", new double[]{26.1608, 119.9519})       // 南竿（馬祖縣政府）
    );

    // Haversine 公式
    public double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 地球半徑 (公里)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // 共用工具
    private Integer getNight(String checkin, String checkout) {
        try {
            if (checkin == null || checkout == null)
                return null;
            java.time.LocalDate in = java.time.LocalDate.parse(checkin);
            java.time.LocalDate out = java.time.LocalDate.parse(checkout);
            return (int) java.time.temporal.ChronoUnit.DAYS.between(in, out);
        } catch (Exception e) {
            return null;
        }
    }
}