package com.example.demo.dto.response;

import java.util.List;

public class ReportSummary {
	private int yearRevenue;
	private int monthRevenue;
	private int totalRevenue;
	private int orderCount;
	private double occupancyRate;
	private double averagePrice;
	private List<ChartData> roomTypeChart;
	private List<ChartData> trendChart;

	public ReportSummary(int yearRevenue,int monthRevenue, int totalRevenue, int orderCount, List<ChartData> roomTypeChart, List<ChartData> trendChart) {
		this.yearRevenue = yearRevenue;
		this.monthRevenue = monthRevenue;
		this.totalRevenue = totalRevenue;
		this.orderCount = orderCount;
		this.roomTypeChart = roomTypeChart;
		this.trendChart = trendChart;
	}

	// getter/setter

	public int getTotalRevenue() {
		return totalRevenue;
	}

	public void setTotalRevenue(int totalRevenue) {
		this.totalRevenue = totalRevenue;
	}

	public int getOrderCount() {
		return orderCount;
	}

	public void setOrderCount(int orderCount) {
		this.orderCount = orderCount;
	}

	public List<ChartData> getRoomTypeChart() {
		return roomTypeChart;
	}

	public void setRoomTypeChart(List<ChartData> roomTypeChart) {
		this.roomTypeChart = roomTypeChart;
	}

	public List<ChartData> getTrendChart() {
		return trendChart;
	}

	public void setTrendChart(List<ChartData> trendChart) {
		this.trendChart = trendChart;
	}

	public int getYearRevenue() {
		return yearRevenue;
	}

	public void setYearRevenue(int yearRevenue) {
		this.yearRevenue = yearRevenue;
	}

	public int getMonthRevenue() {
		return monthRevenue;
	}

	public void setMonthRevenue(int monthRevenue) {
		this.monthRevenue = monthRevenue;
	}

	public double getOccupancyRate() {
		return occupancyRate;
	}

	public void setOccupancyRate(double occupancyRate) {
		this.occupancyRate = occupancyRate;
	}

	public double getAveragePrice() {
		return averagePrice;
	}

	public void setAveragePrice(double averagePrice) {
		this.averagePrice = averagePrice;
	}
}
