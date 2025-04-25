package commands;

import utils.CsvWriter;

public class ExportMapCommand implements Runnable {

    @Override
    public void run() {
        CsvWriter.writeWeatherMapToCsv("Weather map exported");
    }
}