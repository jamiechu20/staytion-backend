package com.example.demo.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.response.ReportSummary;
import com.example.demo.service.StatisticsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/statistics")
@Tag(name = "Admin Statistics Managment", description = "後台報表與統計 API")
public class StatisticsController {

	@Autowired
	private StatisticsService service;

	@Operation(summary = "取得可用年份", description = "回傳目前資料中有訂單的所有年份（依 ownerId 過濾）", operationId = "getAvailableYears")
	@GetMapping("/available-years")
	public List<Integer> getAvailableYears(@RequestParam Long ownerId) {
		return service.getAvailableYears(ownerId);
	}

	@Operation(summary = "取得可用月份", description = "根據指定年份，回傳該年中有訂單的月份（依 ownerId 過濾）", operationId = "getAvailableMonths")
	@GetMapping("/available-months")
	public List<Integer> getAvailableMonths(@RequestParam Long ownerId, @RequestParam int year) {
		return service.getAvailableMonths(ownerId, year);
	}

	@Operation(summary = "取得統計摘要報表", description = "根據年份與月份，回傳總營收、訂單數量、房型統計與營收趨勢圖表（依 ownerId 過濾）", operationId = "getReportSummary")
	@GetMapping("/summary")
	public ReportSummary getReportSummary(@RequestParam Long ownerId, @RequestParam int year, @RequestParam int month) {
		return service.getReportSummary(ownerId, year, month);
	}

	@Operation(summary = "月營收統計", description = "根據指定年份，回傳各月份的訂單總營收資料（給圖表使用，依 ownerId 過濾）", operationId = "summaryMonthRevenue")
	@GetMapping("/summary/monthly-revenue")
	public List<Map<String, Object>> getMonthlyRevenue(@RequestParam Long ownerId, @RequestParam int year) {
		return service.getMonthlyRevenue(ownerId, year);
	}

	@Operation(summary = "訂單趨勢圖", description = "根據日期區間，回傳每天的訂單數量統計資料（給折線圖使用，依 ownerId 過濾）", operationId = "summaryOrderTrend")
	@GetMapping("/summary/order-trend")
	public List<Map<String, Object>> getOrderTrend(@RequestParam Long ownerId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
		return service.getOrderTrend(ownerId, start, end);
	}

	@Operation(summary = "入住率趨勢圖", description = "回傳每日入住率(%)，依 ownerId 與日期區間過濾", operationId = "occupancyRateTrend")
	@GetMapping("/summary/occupancy-rate-trend")
	public List<Map<String, Object>> getOccupancyRateTrend(
			@RequestParam Long ownerId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
		return service.getOccupancyRateTrend(ownerId, start, end);
	}
}
