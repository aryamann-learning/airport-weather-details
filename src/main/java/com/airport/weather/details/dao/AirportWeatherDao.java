package com.airport.weather.details.dao;

import java.util.List;

import com.airport.weather.details.dto.HourlyWeatherDataDto;


public interface AirportWeatherDao {

	public void saveAirportWeatherData(String airport, List<HourlyWeatherDataDto> lst);

}
