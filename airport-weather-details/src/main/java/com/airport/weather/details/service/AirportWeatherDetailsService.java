package com.airport.weather.details.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.airport.weather.details.dto.HourlyWeatherDataDto;


public interface AirportWeatherDetailsService {
	public Map<String, List<HourlyWeatherDataDto>> getAirportWeatherInfo(LocalDate startDate, String cc);
}