package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.example.demo.repository.RoomTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.response.ChartData;
import com.example.demo.dto.response.ReportSummary;
import com.example.demo.repository.OrderRepository;

@Service
public class StatisticsService {

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private RoomTypeRepository roomTypeRepository;

	// 取得有訂單的年份
	public List<Integer> getAvailableYears(Long ownerId) {
		return orderRepository.findAvailableYears(ownerId);
	}

	// 取得該年份有訂單的月份
	public List<Integer> getAvailableMonths(Long ownerId, int year) {
		return orderRepository.findAvailableMonths(ownerId, year);
	}

	// 取得統計報表摘要
	public ReportSummary getReportSummary(Long ownerId, int year, int month) {
		int yearRevenue = Optional.ofNullable(orderRepository.sumYearRevenue(ownerId, year)).orElse(0);
		int monthRevenue = Optional.ofNullable(orderRepository.sumRevenue(ownerId, year, month)).orElse(0);
		int totalRevenue = Optional.ofNullable(orderRepository.sumTotalRevenue(ownerId)).orElse(0);
		int orderCount = Optional.ofNullable(orderRepository.countOrders(ownerId, year, month)).orElse(0);

		List<Map<String, Object>> roomTypeChartRaw = orderRepository.getRoomTypeChart(ownerId, year, month);
		List<Map<String, Object>> trendChartRaw = orderRepository.getTrendChart(ownerId, year, month);

		// 總房數
		Integer totalRooms = roomTypeRepository.sumTotalRoomsByOwner(ownerId);
		if (totalRooms == null || totalRooms == 0)
			totalRooms = 1; // 避免除以零

		// 本月每天訂出去的房數
		LocalDate startDate = LocalDate.of(year, month, 1);
		LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
		List<Map<String, Object>> bookedRoomList = orderRepository.findDailyBookedRooms(ownerId, startDate, endDate);
		Map<LocalDate, Integer> bookedMap = new HashMap<>();
		for (Map<String, Object> row : bookedRoomList) {
			LocalDate date = (LocalDate) row.get("date");
			Integer bookedRooms = ((Number) row.get("bookedRooms")).intValue();
			bookedMap.put(date, bookedRooms);
		}

		// 計算入住率
		int days = endDate.getDayOfMonth();
		int totalBooked = 0;
		for (int day = 1; day <= days; day++) {
			LocalDate date = LocalDate.of(year, month, day);
			totalBooked += bookedMap.getOrDefault(date, 0);
		}
		// 取整數百分比
		double occupancyRate = (days == 0) ? 0 : Math.round(totalBooked * 100.0 / (days * totalRooms));
		// 平均房價
		double averagePrice = (totalBooked == 0) ? 0 : Math.round((double) monthRevenue / totalBooked);

		// 組圖表資料
		List<ChartData> roomTypeChart = roomTypeChartRaw.stream()
				.map(row -> new ChartData((String) row.get("label"), ((Number) row.get("value")).intValue()))
				.collect(Collectors.toList());
		List<ChartData> trendChart = trendChartRaw.stream()
				.map(row -> new ChartData(String.valueOf(row.get("label")), ((Number) row.get("value")).intValue()))
				.collect(Collectors.toList());

		// 回傳 DTO
		ReportSummary summary = new ReportSummary(yearRevenue, monthRevenue, totalRevenue, orderCount, roomTypeChart,
				trendChart);
		summary.setOccupancyRate(occupancyRate);
		summary.setAveragePrice(averagePrice);

		// debug log
		System.out.println(">>> occupancyRate = " + occupancyRate);
		System.out.println(">>> averagePrice = " + averagePrice);

		return summary;
	}

	// 入住率趨勢
	public List<Map<String, Object>> getOccupancyRateTrend(Long ownerId, LocalDate start, LocalDate end) {
		Integer totalRooms = roomTypeRepository.sumTotalRoomsByOwner(ownerId);
		if (totalRooms == null || totalRooms == 0)
			totalRooms = 1;

		List<Map<String, Object>> rawList = orderRepository.findDailyBookedRooms(ownerId, start, end);
		Map<LocalDate, Integer> bookedMap = new HashMap<>();
		for (Map<String, Object> row : rawList) {
			LocalDate date = (LocalDate) row.get("date");
			Integer bookedRooms = ((Number) row.get("bookedRooms")).intValue();
			bookedMap.put(date, bookedRooms);
		}

		List<Map<String, Object>> result = new ArrayList<>();
		LocalDate cur = start;
		while (!cur.isAfter(end)) {
			int booked = bookedMap.getOrDefault(cur, 0);
			int rate = (int) Math.round(booked * 100.0 / totalRooms);
			Map<String, Object> entry = new HashMap<>();
			entry.put("date", cur.toString());
			entry.put("occupancyRate", rate);
			result.add(entry);
			cur = cur.plusDays(1);
		}
		return result;
	}

	// 月營收
	public List<Map<String, Object>> getMonthlyRevenue(Long ownerId, int year) {
		return orderRepository.getMonthlyRevenue(ownerId, year);
	}

	// 每日訂單數
	public List<Map<String, Object>> getOrderTrend(Long ownerId, LocalDate start, LocalDate end) {
		LocalDateTime startDateTime = start.atStartOfDay();
		LocalDateTime endDateTime = end.plusDays(1).atStartOfDay(); // 包含整天

		List<Map<String, Object>> rawData = orderRepository.getOrderTrend(ownerId, startDateTime, endDateTime);

		Map<LocalDate, Long> trendMap = new HashMap<>();
		for (Map<String, Object> row : rawData) {
			LocalDate date = ((java.sql.Date) row.get("date")).toLocalDate();
			Long count = (Long) row.get("orderCount");
			trendMap.put(date, count);
		}

		List<Map<String, Object>> result = new ArrayList<>();
		LocalDate current = start;
		while (!current.isAfter(end)) {
			Long count = trendMap.getOrDefault(current, 0L);
			Map<String, Object> entry = new HashMap<>();
			entry.put("date", current);
			entry.put("orderCount", count);
			result.add(entry);
			current = current.plusDays(1);
		}
		return result;
	}
}