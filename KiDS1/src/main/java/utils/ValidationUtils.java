package utils;

import java.util.Map;
import java.util.Set;

public class ValidationUtils {

    private static final Set<String> LONG_ARGS = Set.of("load-jobs", "min", "max", "letter", "output", "job", "save-jobs");
    private static final Set<String> SHORT_ARGS = Set.of("l", "m", "M", "o", "j", "s");

    public static String mapShortToLongArgument(String shortArg, String command) {
        if (command.equals("START") && shortArg.equals("l")) {
            return "load-jobs";
        }
        if (command.equals("SHUTDOWN") && shortArg.equals("s")) {
            return "save-jobs";
        }
        return switch (shortArg) {
            case "m" -> "min";
            case "M" -> "max";
            case "l" -> "letter";
            case "o" -> "output";
            case "j" -> "job";
            default -> shortArg;
        };
    }

    public static boolean isValidArgumentFormat(boolean isLongFormat, String arg) {
        return isLongFormat ? LONG_ARGS.contains(arg) : SHORT_ARGS.contains(arg);
    }

    public static boolean isValidArgumentForCommand(String command, String arg) {
        return switch (command) {
            case "START" -> arg.equals("load-jobs") || arg.equals("l");
            case "SCAN" -> arg.equals("min") || arg.equals("m") ||
                    arg.equals("max") || arg.equals("M") ||
                    arg.equals("letter") || arg.equals("l") ||
                    arg.equals("output") || arg.equals("o") ||
                    arg.equals("job") || arg.equals("j");
            case "MAP", "EXPORTMAP" -> false;
            case "STATUS" -> arg.equals("job") || arg.equals("j");
            case "SHUTDOWN" -> arg.equals("save-jobs") || arg.equals("s");
            default -> true;
        };
    }

    public static boolean requiresValue(String command, String arg) {
        return switch (command) {
            case "SCAN" -> true;
            case "STATUS" -> arg.equals("job");
            default -> false;
        };
    }

    public static boolean isValidStart(Map<String, String> arguments) {
        if (arguments.isEmpty()) {
            return true;
        }
        return arguments.size() == 1 && arguments.containsKey("load-jobs");
    }

    public static boolean isValidScan(Map<String, String> arguments) {
        if (arguments.size() != 5) {
            return false;
        }
        if (!arguments.containsKey("min") || !arguments.containsKey("max") ||
                !arguments.containsKey("letter") || !arguments.containsKey("output") ||
                !arguments.containsKey("job")) {
            return false;
        }

        try {
            Double.parseDouble(arguments.get("min"));
            Double.parseDouble(arguments.get("max"));
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format for --min or --max");
            return false;
        }

        String letter = arguments.get("letter");
        if (letter.length() != 1) {
            System.err.println("Invalid value for --letter: must be a single character");
            return false;
        }

        return true;
    }

    public static boolean isValidStatus(Map<String, String> arguments) {
        return arguments.size() == 1 && arguments.containsKey("job");
    }
}