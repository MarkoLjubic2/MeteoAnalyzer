package weather;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Getter
public class WeatherData {
    private final AtomicInteger stationCount = new AtomicInteger(0);
    private final AtomicReference<Double> totalTemperature = new AtomicReference<>(0.0);

    public void addStation(double temperature) {
        stationCount.incrementAndGet();
        totalTemperature.updateAndGet(current -> current + temperature);
    }
}