package commands;

import global.GlobalData;
import weather.WeatherData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapCommand implements Runnable {

    @Override
    public void run() {
        if (GlobalData.getInstance().isWeatherMapUpdating()) {
            System.out.println("Weather map is currently being updated or has pending tasks. Please try again later.");
            return;
        }

        Map<Character, WeatherData> weatherMap = GlobalData.getInstance().getWeatherMap();
        if (weatherMap.isEmpty()) {
            System.out.println("Map is not available yet");
            return;
        }

        List<Character> keys = new ArrayList<>(weatherMap.keySet());

        for (int i = 0; i < keys.size(); i += 2) {
            StringBuilder line = new StringBuilder();

            char letter1 = keys.get(i);
            WeatherData data1 = weatherMap.get(letter1);
            line.append(letter1).append(": ")
                    .append(data1.getStationCount()).append(" - ")
                    .append(data1.getTotalTemperature());

            if (i + 1 < keys.size()) {
                char letter2 = keys.get(i + 1);
                WeatherData data2 = weatherMap.get(letter2);
                line.append(" | ").append(letter2).append(": ")
                        .append(data2.getStationCount()).append(" - ")
                        .append(data2.getTotalTemperature());
            }

            System.out.println(line);
        }
    }
}