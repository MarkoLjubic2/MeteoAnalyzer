package commands;

import global.GlobalData;
import lombok.Getter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

@Getter
public class ScanManager implements Runnable {
    private final double minTemp;
    private final double maxTemp;
    private final char letter;
    private final String outputFileName;
    private final String jobName;
    private final Path directory;

    public ScanManager(double minTemp, double maxTemp, char letter, String outputFileName, String jobName, Path directory) {
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.letter = letter;
        this.outputFileName = outputFileName;
        this.jobName = jobName;
        this.directory = directory;
        GlobalData.getInstance().updateTaskStatus(jobName, GlobalData.TaskStatus.PENDING);
    }

    @Override
    public void run() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName, true))) {
            List<Path> files = Files.list(directory).toList();
            CountDownLatch latch = new CountDownLatch(files.size());

            files.forEach(filePath -> {
                GlobalData.getInstance().getFileLocks().putIfAbsent(filePath, new ReentrantLock());
                SingleFileScan scanTask = new SingleFileScan(filePath, minTemp, maxTemp, letter, writer, latch, jobName);
                try {
                    GlobalData.getInstance().getTasks().put(scanTask);
                } catch (Exception e) {
                    System.err.println("Error submitting scan task for file: " + filePath + " - " + e.getMessage());
                }
            });

            latch.await();
            GlobalData.getInstance().updateTaskStatus(jobName, GlobalData.TaskStatus.COMPLETED);
            System.out.println("All files processed for job: " + jobName);
        } catch (IOException e) {
            System.err.println("Error writing to output file: " + outputFileName + " - " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("ScanJob interrupted for job: " + jobName + " - " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public String toString() {
        return String.format("SCAN --min %f --max %f --letter %c --output %s --job %s",
                minTemp, maxTemp, letter, outputFileName, jobName);
    }
}