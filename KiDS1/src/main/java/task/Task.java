package task;

import global.GlobalData;
import lombok.AllArgsConstructor;
import weather.WeatherData;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantLock;

@AllArgsConstructor
public class Task implements Runnable {
    private final Path filePath;

    @Override
    public void run() {
        ReentrantLock lock = GlobalData.getInstance().getFileLocks().get(filePath);
        if (lock != null) {
            lock.lock();
            System.out.println("Task started for file: " + filePath);
            try {
                try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
                    String line;
                    boolean isFirstLine = true;

                    while ((line = reader.readLine()) != null) {
                        if (isFirstLine && filePath.toString().endsWith(".csv")) {
                            isFirstLine = false;
                            System.out.println("Skipping first line of CSV file: " + filePath);
                            continue;
                        }

                        String[] parts = line.split(";");
                        if (parts.length >= 2) {
                            String stationName = parts[0].trim();
                            double temperature = Double.parseDouble(parts[1].trim());

                            char firstLetter = stationName.toUpperCase().charAt(0);
                            GlobalData.getInstance().getWeatherMap().compute(firstLetter, (key, weatherData) -> {
                                if (weatherData == null) {
                                    weatherData = new WeatherData();
                                }
                                weatherData.addStation(temperature);
                                return weatherData;
                            });
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error reading file: " + filePath + " - " + e.getMessage());
                }
            } finally {
                System.out.println("Task completed for file: " + filePath);
                GlobalData.getInstance().getTotalWeatherTasks().decrementAndGet();
                lock.unlock();
            }
        } else {
            System.err.println("Lock not found for file: " + filePath);
        }
    }

}