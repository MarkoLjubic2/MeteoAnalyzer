# Meteo Analyzer

This project implements a concurrent file processing system for handling `.txt` and `.csv` files containing meteorological data. It monitors a specified directory, processes files with temperature readings from weather stations, and provides a command-line interface for job execution and system inspection.

## Features

- **Directory Monitoring**  
  Watches a target directory and detects added or modified `.txt` and `.csv` files.

- **Concurrent File Processing**  
  Processes files using a `ExecutorService`. Each file is handled by a separate thread.

- **In-Memory Aggregation Map**  
  Maintains a synchronized map indexed by the first letter of station names. For each letter:
  - Counts the number of stations
  - Sums all temperature measurements

- **Command-Line Interface (CLI)**  
  Accepts commands via standard input and delegates tasks through a blocking queue.

## CLI Commands

### `START`  
Starts the system.  
Optional argument:
- `--load-jobs` or `-l`: Loads pre-saved jobs from a `load_config` file.

### `SCAN`  
Searches files for station names starting with a given letter and temperatures in a specified range.  
Arguments:
- `--min` or `-m`: Minimum temperature  
- `--max` or `-M`: Maximum temperature  
- `--letter` or `-l`: Starting letter of station names  
- `--output` or `-o`: Output file name  
- `--job` or `-j`: Unique job identifier

### `STATUS`  
Displays the status of a given job.  
Arguments:
- `--job` or `-j`: Job name

### `MAP`  
Prints the current contents of the in-memory aggregation map. If the map is not yet initialized, a message is shown.

Exports the current in-memory map to a CSV log file.

**CSV Format:**

| Letter | Station count | Sum     |
|--------|----------------|---------|
| A      | 123            | 4567    |
| B      | 234            | 8910    |
| ...    | ...            | ...     |


## Periodic Reporting

A background thread generates a report every minute, writing the current in-memory map state to the same CSV file used by `EXPORTMAP`. Synchronization prevents conflicts between manual and automatic logging.

## Notes

- Uses a fixed thread pool with 4 threads for file processing.
- File access and map updates are synchronized to ensure thread safety.
- All commands are parsed and validated, invalid commands do not block the system.
- Each job has a unique ID used to track its status (pending, running, completed).

## File Format

Input files must have a `.txt` or `.csv` extension.

Each line should follow this format:

`StationName;Temperature`

**Example:**

```csv
Belgrade;12.0
Boston;6.2
