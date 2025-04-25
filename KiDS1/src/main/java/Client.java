import global.GlobalData;

import java.util.*;

public class Client implements Runnable {

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("exit")) {
                break;
            }
            try {
                GlobalData.getInstance().getCommands().put(input);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        scanner.close();
    }
}