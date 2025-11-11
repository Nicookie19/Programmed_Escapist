package progescps;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class InputProvider {
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    
    public String nextLine() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "";
        }
    }

    
    public void submit(String line) {
        if (line == null) line = "";
        try {
            queue.put(line);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    
    public void submitTrimmed(String line) {
        submit(line == null ? "" : line.trim());
    }

    
    public void clearPending() {
        queue.clear();
    }
}