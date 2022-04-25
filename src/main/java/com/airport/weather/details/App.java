package com.airport.weather.details;

import java.time.LocalDate;

import com.airport.weather.details.service.AirportWeatherDetailsService;
import com.airport.weather.details.service.AirportWeatherDetailsServiceImpl;

public class App {
	public static void main(String[] args) {
		AirportWeatherDetailsService airportWeatherDetailsService = new AirportWeatherDetailsServiceImpl();
		airportWeatherDetailsService.getAirportWeatherInfo(LocalDate.now(), "01");
	}
}
