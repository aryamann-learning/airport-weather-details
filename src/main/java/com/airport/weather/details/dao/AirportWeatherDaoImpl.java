package com.airport.weather.details.dao;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Properties;

import com.airport.weather.details.dto.HourlyWeatherDataDto;

public class AirportWeatherDaoImpl implements AirportWeatherDao {

	@Override
	public void saveAirportWeatherData(String airport, List<HourlyWeatherDataDto> lstHourlyWeatherDataDto) {
		try {
			FileReader reader = new FileReader("src\\main\\resources\\config.properties");
			Properties prop = new Properties();
			prop.load(reader);
			final String insertQuery = "insert into FPS_WEATHER_DATA(AIRPORT_ICAO_CD,READING_DT,READING_HOUR_NBR,TEMPERATURE_NBR,"
					+ "WIND_DIRECTION_NBR, WIND_SPEED_NBR,WIND_GUST_NBR,PERCIPITATION_CHANGE_PCT,CEILING_HEIGHT_NBR,VISIBILITY_NBR) "
					+ "values (?,?,?,?,?,?,?,?,?,?)" + "ON DUPLICATE KEY "
					+ "UPDATE TEMPERATURE_NBR=TEMPERATURE_NBR, WIND_DIRECTION_NBR=WIND_DIRECTION_NBR, WIND_SPEED_NBR=WIND_SPEED_NBR, WIND_GUST_NBR=WIND_GUST_NBR,"
					+ " PERCIPITATION_CHANGE_PCT=PERCIPITATION_CHANGE_PCT, CEILING_HEIGHT_NBR=CEILING_HEIGHT_NBR, VISIBILITY_NBR=VISIBILITY_NBR";
			try (Connection conn = DriverManager.getConnection(prop.getProperty("SOURCE_URL"),
					prop.getProperty("USERNAME"), prop.getProperty("PASSWORD"));
					PreparedStatement psInsert = conn.prepareStatement(insertQuery);) {

				for (HourlyWeatherDataDto hourlyWeatherDataDto : lstHourlyWeatherDataDto) {
					psInsert.setString(1, airport);
					psInsert.setDate(2, Date.valueOf(hourlyWeatherDataDto.getDate()));
					psInsert.setInt(3, hourlyWeatherDataDto.getHourlyForecast());
					if (hourlyWeatherDataDto.getTemperature() != null)
						psInsert.setInt(4, hourlyWeatherDataDto.getTemperature());
					else
						psInsert.setNull(4, Types.INTEGER);

					if (hourlyWeatherDataDto.getWindDirection() != null)
						psInsert.setInt(5, hourlyWeatherDataDto.getWindDirection());
					else
						psInsert.setNull(5, Types.INTEGER);

					if (hourlyWeatherDataDto.getWindSpeed() != null)
						psInsert.setInt(6, hourlyWeatherDataDto.getWindSpeed());
					else
						psInsert.setNull(6, Types.INTEGER);

					if (hourlyWeatherDataDto.getWindGust() != null)
						psInsert.setInt(7, hourlyWeatherDataDto.getWindGust());
					else
						psInsert.setNull(7, Types.INTEGER);

					if (hourlyWeatherDataDto.getPrecipChance() != null)
						psInsert.setInt(8, hourlyWeatherDataDto.getPrecipChance());
					else
						psInsert.setNull(8, Types.INTEGER);

					if (hourlyWeatherDataDto.getCeilingHeight() != null)
						psInsert.setInt(9, hourlyWeatherDataDto.getCeilingHeight());
					else
						psInsert.setNull(9, Types.INTEGER);

					if (hourlyWeatherDataDto.getVisibility() != null)
						psInsert.setInt(10, hourlyWeatherDataDto.getVisibility());
					else
						psInsert.setNull(10, Types.INTEGER);
					psInsert.addBatch();
				}

				psInsert.executeBatch();

			} catch (SQLException e) {
				System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
