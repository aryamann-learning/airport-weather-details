package com.airport.weather.details.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.airport.weather.details.dao.AirportWeatherDao;
import com.airport.weather.details.dao.AirportWeatherDaoImpl;
import com.airport.weather.details.dto.HourlyWeatherDataDto;

public class AirportWeatherDetailsServiceImpl implements AirportWeatherDetailsService {

	public static final String SPLIT_BY_3CHARS_REGEX = "(?<=\\G.{3})";
	LinkedHashMap<String, List<HourlyWeatherDataDto>> mapHourlyWeatherData = new LinkedHashMap<>();
	private AirportWeatherDao daoAirportWeather;
	private List<String> lstAirports;

	public AirportWeatherDetailsServiceImpl() {
		this.daoAirportWeather = new AirportWeatherDaoImpl();
		this.lstAirports = Arrays.asList("KMEM", "KSFO");
	}

	private String getWebClientResponse(String url) {
		String responseString = null;
		HttpGet request = new HttpGet(url);
		try (CloseableHttpClient httpClient = HttpClients.createDefault();
				CloseableHttpResponse response = httpClient.execute(request);) {
			HttpEntity entity = response.getEntity();
			responseString = EntityUtils.toString(entity);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return responseString;

	}

	@Override
	public Map<String, List<HourlyWeatherDataDto>> getAirportWeatherInfo(LocalDate startDate, String cc) {
		ExecutorService executorService = Executors.newFixedThreadPool(25);
		String url = getUrl(startDate, cc);
		String response = getWebClientResponse(url);
		// System.out.println(response);
		LocalDate endDate = getDate(startDate);
		String[] airports = response.split("\n\\s*\n");
		System.out.println("run started...");
		long startTime = System.nanoTime();
		for (int i = 1; i < airports.length; i++) {
			executorService.execute(newRunnable(airports[i], startDate, endDate));
		}
		executorService.shutdown();
		while (!executorService.isTerminated()) {
		}
		System.out.println("run completed...");
		long endTime = System.nanoTime();
		long totalTime = endTime - startTime;
		System.out.println(totalTime);
		return mapHourlyWeatherData;
	}

	public Runnable newRunnable(final String airportBlock, final LocalDate startDate, final LocalDate endDate) {

		return new Runnable() {

			@Override
			public void run() {
				parseBlendText(airportBlock, startDate, endDate);
			}
		};
	}

	public void parseBlendText(String airportBlock, LocalDate startDate, LocalDate endDate) {
		String[] utc = null, tmp = null, wdr = null, wsp = null, gst = null, p01 = null, cig = null, vis = null;
		String line = "";
		try (InputStream stream = new ByteArrayInputStream(airportBlock.getBytes(Charset.forName("UTF-8")));
				BufferedReader br = new BufferedReader(new InputStreamReader(stream));) {
			line = br.readLine().trim();
			String airport = line.substring(0, line.indexOf(" "));
			if (lstAirports.contains(airport)) {
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if (line.contains("UTC")) {
						utc = getHourlyData(line);
					} else if (line.startsWith("TMP")) {
						tmp = getHourlyData(line);
					} else if (line.startsWith("WDR")) {
						wdr = getHourlyData(line);
					} else if (line.startsWith("WSP")) {
						wsp = getHourlyData(line);
					} else if (line.startsWith("GST")) {
						gst = getHourlyData(line);
					} else if (line.startsWith("P01")) {
						p01 = getHourlyData(line);
					} else if (line.startsWith("CIG")) {
						cig = getHourlyData(line);
					} else if (line.startsWith("VIS")) {
						vis = getHourlyData(line);
					}
				}
				List<HourlyWeatherDataDto> lstHourlyData = getLstHourlyData(airport, startDate, endDate, utc, tmp, wdr,
						wsp, gst, p01, cig, vis);
				mapHourlyWeatherData.put(airport, lstHourlyData);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<HourlyWeatherDataDto> getLstHourlyData(String airport, LocalDate startDate, LocalDate endDate,
			String[] utc, String[] tmp, String[] wdr, String[] wsp, String[] gst, String[] p01, String[] cig,
			String[] vis) {
		List<HourlyWeatherDataDto> lstHourlyData = new ArrayList<>();
		LocalDate date = startDate;
		for (int i = 0; i < utc.length; i++) {
			HourlyWeatherDataDto hourlyWeatherDataDto = new HourlyWeatherDataDto();
			hourlyWeatherDataDto.setHourlyForecast(Integer.parseInt(utc[i].trim()));
			if (tmp != null)
				hourlyWeatherDataDto.setTemperature(Integer.parseInt(tmp[i].trim()));
			if (wdr != null)
				hourlyWeatherDataDto.setWindDirection(Integer.parseInt(wdr[i].trim()));
			if (wsp != null)
				hourlyWeatherDataDto.setWindSpeed(Integer.parseInt(wsp[i].trim()));
			if (gst != null)
				hourlyWeatherDataDto.setWindGust(Integer.parseInt(gst[i].trim()));
			if (p01 != null)
				hourlyWeatherDataDto.setPrecipChance(Integer.parseInt(p01[i].trim()));
			if (cig != null)
				hourlyWeatherDataDto.setCeilingHeight(Integer.parseInt(cig[i].trim()));
			if (vis != null)
				hourlyWeatherDataDto.setVisibility(Integer.parseInt(vis[i].trim()));
			hourlyWeatherDataDto.setDate(date);
			lstHourlyData.add(hourlyWeatherDataDto);
			if (hourlyWeatherDataDto.getHourlyForecast() == 23) {
				date = endDate;
			}
		}
		daoAirportWeather.saveAirportWeatherData(airport, lstHourlyData);
		return lstHourlyData;
	}

	private String getUrl(LocalDate startDate, String cc) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		String date = startDate.format(formatter);
		String url = "https://nomads.ncep.noaa.gov/pub/data/nccf/com/blend/prod/blend.:date/:cc/text/blend_nbhtx.t:ccz";
		url = url.replace(":date", date).replace(":cc", cc);
		return url;
	}

	private String[] getHourlyData(String line) {
		String arrLine = line.substring(4, line.length());
		return arrLine.split(SPLIT_BY_3CHARS_REGEX);
	}

	private LocalDate getDate(LocalDate startDate) {
		String formattedDate = startDate.plusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		LocalDate localDate = LocalDate.parse(formattedDate, formatter);
		return localDate;
	}

}
