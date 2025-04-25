package commands;

import global.GlobalData;

public class StatusCommand implements Runnable {
    private final String jobName;

    public StatusCommand(String jobName) {
        this.jobName = jobName;
    }

    @Override
    public void run() {
        GlobalData.TaskStatus status = GlobalData.getInstance().getTaskStatus(jobName);
        if (status == null) {
            System.out.println("Job '" + jobName + "' not found");
        } else {
            System.out.println(jobName + " is " + status.name());
        }
    }
}