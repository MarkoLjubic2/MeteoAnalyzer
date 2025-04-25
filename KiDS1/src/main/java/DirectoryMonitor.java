import global.GlobalData;
import lombok.AllArgsConstructor;
import task.Task;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.locks.ReentrantLock;

@AllArgsConstructor
public class DirectoryMonitor implements Runnable {
    private final Path directory;

    @Override
    public void run() {
        try {
            processAllFiles();

            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                directory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);

                while (true) {

                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        Path filePath = directory.resolve((Path) event.context());

                        if (!filePath.toString().endsWith(".txt") && !filePath.toString().endsWith(".csv")) {
                            continue;
                        }
                        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                            System.out.println("New file detected: " + filePath);
                        } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                            System.out.println("File modified: " + filePath);
                        }

                        GlobalData.getInstance().getWeatherMap().clear();
                        processAllFiles();
                    }
                    key.reset();
                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error monitoring directory: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private void processAllFiles() {
        try {
            Files.list(directory)
                    .filter(path -> path.toString().endsWith(".txt") || path.toString().endsWith(".csv"))
                    .forEach(filePath -> {
                        GlobalData.getInstance().getFileLocks().putIfAbsent(filePath, new ReentrantLock());
                        try {
                            GlobalData.getInstance().getTotalWeatherTasks().incrementAndGet();
                            GlobalData.getInstance().getTasks().put(new Task(filePath));
                        } catch (InterruptedException e) {
                            System.err.println("Error adding file to queue: " + filePath + " - " + e.getMessage());
                            Thread.currentThread().interrupt();
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error reading directory: " + e.getMessage());
        }
    }
}