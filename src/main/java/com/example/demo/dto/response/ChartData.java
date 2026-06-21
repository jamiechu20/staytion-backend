package com.example.demo.dto.response;

public class ChartData {
	private String label;
	private int value;

	public ChartData(String label, int value) {
		this.label = label;
		this.value = value;
	}

	// getter/setter

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

}
