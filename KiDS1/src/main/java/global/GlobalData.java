package global;

import lombok.Getter;
import weather.WeatherData;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Getter
public class GlobalData {

    private static class Holder {
        private static final GlobalData INSTANCE = new GlobalData();
    }

    private GlobalData() {
    }

    public static GlobalData getInstance() {
        return Holder.INSTANCE;
    }

    private final Path outputPath = Paths.get("log.csv");

    private final BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();
    private final BlockingQueue<String> commands = new LinkedBlockingQueue<>();

    private final Map<Path, ReentrantLock> fileLocks = new ConcurrentHashMap<>();
    private final Map<Character, WeatherData> weatherMap = new ConcurrentSkipListMap<>();

    private final ScheduledExecutorService csvSchedulerService = Executors.newScheduledThreadPool(1);
    private final ReentrantLock csvFileLock = new ReentrantLock();

    private final ExecutorService fileExecutorService = Executors.newFixedThreadPool(4);
    private final ExecutorService componentExecutorService = Executors.newFixedThreadPool(4);
    private final ExecutorService scanExecutorService = Executors.newFixedThreadPool(10);

    private final ExecutorService commandExecutorService = Executors.newFixedThreadPool(10);

    private final AtomicInteger totalWeatherTasks = new AtomicInteger(0);

    private final Map<String, AtomicBoolean> jobStarted = new ConcurrentHashMap<>();

    private final Map<String, TaskStatus> taskStatuses = new ConcurrentHashMap<>();

    public enum TaskStatus {
        PENDING, RUNNING, COMPLETED
    }

    public void updateTaskStatus(String jobName, TaskStatus status) {
        taskStatuses.put(jobName, status);
    }

    public TaskStatus getTaskStatus(String jobName) {
        return taskStatuses.getOrDefault(jobName, null);
    }

    public boolean isWeatherMapUpdating() {
        return totalWeatherTasks.get() > 0;
    }

    public boolean markJobAsStarted(String jobName) {
        return jobStarted.computeIfAbsent(jobName, k -> new AtomicBoolean(false)).compareAndSet(false, true);
    }
}