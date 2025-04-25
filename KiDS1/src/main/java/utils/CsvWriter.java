package utils;

import global.GlobalData;
import weather.WeatherData;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class CsvWriter {

    public static void writeWeatherMapToCsv(String messagePrefix) {
        if (GlobalData.getInstance().isWeatherMapUpdating()) {
            System.out.println(messagePrefix + ": Weather map is currently being updated or has pending tasks. Skipping this report.");
            return;
        }

        Map<Character, WeatherData> weatherMap = GlobalData.getInstance().getWeatherMap();
        if (weatherMap.isEmpty()) {
            System.out.println(messagePrefix + ": Map is not available yet");
            return;
        }

        ReentrantLock csvLock = GlobalData.getInstance().getCsvFileLock();
        csvLock.lock();
        try (BufferedWriter writer = Files.newBufferedWriter(GlobalData.getInstance().getOutputPath())) {
            writer.write("Letter,Station count,Sum");
            writer.newLine();

            for (Map.Entry<Character, WeatherData> entry : weatherMap.entrySet()) {
                char letter = entry.getKey();
                WeatherData data = entry.getValue();
                String line = String.format("%c,%d,%f",
                        letter,
                        data.getStationCount().get(),
                        data.getTotalTemperature().get());
                writer.write(line);
                writer.newLine();
            }
            System.out.println(messagePrefix + ": Weather map exported to " + GlobalData.getInstance().getOutputPath());
        } catch (IOException e) {
            System.err.println(messagePrefix + ": Error writing to CSV file: " + e.getMessage());
        } finally {
            csvLock.unlock();
        }
    }
}
