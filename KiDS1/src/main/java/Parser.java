import commands.ExportMapCommand;
import commands.MapCommand;
import commands.ScanManager;
import commands.StatusCommand;
import global.GlobalData;
import processors.Processor;
import utils.CsvWriter;
import utils.ValidationUtils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Parser implements Runnable {
    private volatile boolean isProcessorStarted = false;

    @Override
    public void run() {
        while (true) {
            try {
                String input = GlobalData.getInstance().getCommands().take();
                Map<String, String> arguments = parseCommand(input);
                String command = input.split(" ")[0].toUpperCase();

                if (arguments == null) {
                    System.err.println("Invalid command format for " + command + ". Check arguments.");
                    continue;
                }

                if (!isProcessorStarted && !command.equals("START") && !command.equals("SHUTDOWN")) {
                    GlobalData.getInstance().getCommands().put(input);
                    continue;
                }

                switch (command) {
                    case "START":
                        if (!ValidationUtils.isValidStart(arguments)) {
                            System.err.println("Invalid START command. Usage: START [--load-jobs] (or short form: -l)");
                            break;
                        }
                        handleStartCommand(arguments);
                        break;
                    case "SCAN":
                        if (!ValidationUtils.isValidScan(arguments)) {
                            System.err.println("Invalid SCAN command. Usage: SCAN --min <value> --max <value> --letter <char> --output <file> --job <name> (or short form: -m -M -l -o -j)");
                            break;
                        }
                        handleScanCommand(arguments);
                        break;
                    case "MAP":
                        if (!arguments.isEmpty()) {
                            System.err.println("Invalid MAP command. Usage: MAP (no arguments allowed)");
                            break;
                        }
                        handleMapCommand();
                        break;
                    case "EXPORTMAP":
                        if (!arguments.isEmpty()) {
                            System.err.println("Invalid EXPORTMAP command. Usage: EXPORTMAP (no arguments allowed)");
                            break;
                        }
                        handleExportMapCommand();
                        break;
                    case "STATUS":
                        if (!ValidationUtils.isValidStatus(arguments)) {
                            System.err.println("Invalid STATUS command. Usage: STATUS --job <name> (or short form: -j)");
                            break;
                        }
                        handleStatusCommand(arguments);
                        break;
                    default:
                        System.err.println("Unknown command: " + command);
                }
            } catch (InterruptedException e) {
                System.err.println("Parser interrupted");
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private Map<String, String> parseCommand(String input) {
        String[] parts = input.split(" ");
        Map<String, String> arguments = new HashMap<>();
        if (parts.length == 1) {
            return arguments;
        }

        String command = parts[0].toUpperCase();
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].startsWith("--") || parts[i].startsWith("-")) {
                boolean isLongFormat = parts[i].startsWith("--");
                String key = parts[i].substring(isLongFormat ? 2 : 1);
                String originalKey = key;

                if (!ValidationUtils.isValidArgumentFormat(isLongFormat, originalKey)) {
                    System.err.println("Invalid argument format: " + parts[i] + " (use -- for long arguments, - for short arguments)");
                    return null;
                }

                key = ValidationUtils.mapShortToLongArgument(originalKey, command);

                if (!ValidationUtils.isValidArgumentForCommand(command, originalKey)) {
                    System.err.println("Invalid argument for " + command + ": " + parts[i]);
                    return null;
                }

                boolean requiresValue = ValidationUtils.requiresValue(command, key);
                if (requiresValue) {
                    if (i + 1 < parts.length) {
                        String nextPart = parts[i + 1];
                        if (nextPart.startsWith("--") || (nextPart.startsWith("-") && isArgument(nextPart, command))) {
                            System.err.println("Missing value for argument: " + parts[i]);
                            return null;
                        }
                        arguments.put(key, nextPart);
                        i++;
                    } else {
                        System.err.println("Missing value for argument: " + parts[i]);
                        return null;
                    }
                } else {
                    arguments.put(key, "");
                }
            } else {
                System.err.println("Invalid argument format: " + parts[i] + " (must start with -- or -)");
                return null;
            }
        }
        return arguments;
    }

    private boolean isArgument(String part, String command) {
        if (part.startsWith("--")) {
            return true;
        }
        if (part.startsWith("-") && part.length() > 1) {
            String shortArg = part.substring(1);
            return ValidationUtils.isValidArgumentForCommand(command, shortArg);
        }
        return false;
    }

    private void handleStatusCommand(Map<String, String> arguments) {
        System.out.println("Handling STATUS command");
        String jobName = arguments.get("job");
        try {
            GlobalData.getInstance().getTasks().put(new StatusCommand(jobName));
        } catch (InterruptedException e) {
            System.err.println("Error adding StatusCommand to queue: " + e.getMessage());
        }
    }

    private void handleScanCommand(Map<String, String> arguments) {
        System.out.println("Handling SCAN command");
        double minTemp = Double.parseDouble(arguments.get("min"));
        double maxTemp = Double.parseDouble(arguments.get("max"));
        char letter = arguments.get("letter").charAt(0);
        String outputFileName = arguments.get("output");
        String jobName = arguments.get("job");
        Path directory = Paths.get("src/main/resources");

        ScanManager scanManager = new ScanManager(minTemp, maxTemp, letter, outputFileName, jobName, directory);
        GlobalData.getInstance().getScanExecutorService().submit(scanManager);
    }

    private void handleStartCommand(Map<String, String> arguments) {
        System.out.println("Handling START command");

        GlobalData.getInstance().getCsvSchedulerService().scheduleAtFixedRate(
                () -> CsvWriter.writeWeatherMapToCsv("Periodic report"),
                60, 60, TimeUnit.SECONDS);

        if (arguments.containsKey("load-jobs")) {
            try (BufferedReader reader = new BufferedReader(new FileReader("load_config.txt"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    GlobalData.getInstance().getCommands().put(line);
                }
                System.out.println("Jobs loaded from load_config");
                System.out.println("Commands: " + GlobalData.getInstance().getCommands());
            } catch (IOException | InterruptedException e) {
                System.err.println("Error loading jobs: " + e.getMessage());
                return;
            }
        }

        if (!isProcessorStarted) {
            GlobalData.getInstance().getComponentExecutorService().submit(new Processor());
            GlobalData.getInstance().getTasks().add(new DirectoryMonitor(Paths.get("src/main/resources")));
            isProcessorStarted = true;
        }
    }

    private void handleMapCommand() {
        System.out.println("Handling MAP command");
        try {
            GlobalData.getInstance().getTasks().put(new MapCommand());
        } catch (InterruptedException e) {
            System.err.println("Error adding MapCommand to queue: " + e.getMessage());
        }
    }

    private void handleExportMapCommand() {
        System.out.println("Handling EXPORTMAP command");
        try {
            GlobalData.getInstance().getTasks().put(new ExportMapCommand());
        } catch (InterruptedException e) {
            System.err.println("Error adding ExportMapCommand to queue: " + e.getMessage());
        }
    }

}