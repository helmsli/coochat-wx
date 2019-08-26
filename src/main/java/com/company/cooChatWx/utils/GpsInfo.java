package com.company.cooChatWx.utils;

import com.company.cooChatWx.utils.BaseDomain;

public class GpsInfo extends BaseDomain {

	private static final long serialVersionUID = 1L;

	// 经度
	private double longitude;

	// 维度
	private double latitude;

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
}
