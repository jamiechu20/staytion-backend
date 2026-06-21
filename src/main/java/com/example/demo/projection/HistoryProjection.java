package com.example.demo.projection;

import java.time.LocalDate;

public interface HistoryProjection {
	String getLocationName();  // 可以是 city 或 district 名稱

	String getImgUrl();

	LocalDate getCheckInDate();

	LocalDate getCheckOutDate();

	Integer getTotal();
}
