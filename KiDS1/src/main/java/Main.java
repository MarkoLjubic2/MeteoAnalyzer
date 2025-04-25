import global.GlobalData;

public class Main {

    public static void main(String[] args) {

        GlobalData.getInstance().getComponentExecutorService().submit(new Client());
        GlobalData.getInstance().getComponentExecutorService().submit(new Parser());
    }
}