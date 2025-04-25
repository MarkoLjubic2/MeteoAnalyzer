package commands;

import global.GlobalData;
import lombok.AllArgsConstructor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

@AllArgsConstructor
public class SingleFileScan implements Runnable {

    private final Path filePath;
    private final double minTemp;
    private final double maxTemp;
    private final char letter;
    private final BufferedWriter writer;
    private final CountDownLatch latch;
    private final String jobName;

    @Override
    public void run() {
        ReentrantLock lock = GlobalData.getInstance().getFileLocks().get(filePath);
        lock.lock();
        System.out.println("SingleFileScan started for file: " + filePath + " in job: " + jobName);
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            if (GlobalData.getInstance().markJobAsStarted(jobName)) {
                GlobalData.getInstance().updateTaskStatus(jobName, GlobalData.TaskStatus.RUNNING);
            }
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine && filePath.toString().endsWith(".csv")) {
                    isFirstLine = false;
                    continue;
                }

                String[] parts = line.split(";");
                if (parts.length >= 2) {
                    String stationName = parts[0].trim();
                    double temperature = Double.parseDouble(parts[1].trim());

                    if (stationName.toUpperCase().charAt(0) == letter && temperature >= minTemp && temperature <= maxTemp) {

                        synchronized (writer) {
                            writer.write(stationName + ";" + temperature);
                            writer.newLine();
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + filePath + " - " + e.getMessage());
        } finally {
            System.out.println("SingleFileScan completed for " + filePath + " in job: " + jobName +" "+ latch.getCount());
            lock.unlock();
            latch.countDown();
        }
    }

}