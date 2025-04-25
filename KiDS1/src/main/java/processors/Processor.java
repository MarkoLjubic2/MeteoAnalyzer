package processors;

import commands.ExportMapCommand;
import commands.MapCommand;
import commands.SingleFileScan;
import commands.StatusCommand;
import global.GlobalData;
import task.Task;

public class Processor implements Runnable {

    @Override
    public void run() {
        while (true) {
            try {
                Runnable task = GlobalData.getInstance().getTasks().take();

                if (task instanceof Task || task instanceof SingleFileScan) {
                    GlobalData.getInstance().getFileExecutorService().submit(task);
                } else if (task instanceof MapCommand || task instanceof ExportMapCommand || task instanceof StatusCommand) {
                    GlobalData.getInstance().getCommandExecutorService().submit(task);
                } else{
                    GlobalData.getInstance().getComponentExecutorService().submit(task);
                }
            } catch (InterruptedException e) {
                System.err.println("Task processing interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}