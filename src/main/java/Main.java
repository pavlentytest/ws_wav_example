import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class Main {
    private ArrayList<String> results = new ArrayList<String>();
    private CountDownLatch recieveLatch;

    public ArrayList<String> transcribe(String path) throws Exception {
        WebSocketFactory factory = new WebSocketFactory();
        WebSocket ws = factory.createSocket("ws://178.154.207.26:2700");
        ws.addListener(new WebSocketAdapter() {
            @Override
            public void onTextMessage(WebSocket websocket, String message) {
                results.add(message);
                recieveLatch.countDown();
            }
        });
        ws.connect();

        FileInputStream fis = new FileInputStream(new File(path));
        DataInputStream dis = new DataInputStream(fis);
        byte[] buf = new byte[8000];
        while (true) {
            int nbytes = dis.read(buf);
            if (nbytes < 0) break;
            recieveLatch = new CountDownLatch(1);
            ws.sendBinary(buf);
            recieveLatch.await();
        }
        recieveLatch = new CountDownLatch(1);
        ws.sendText("{\"eof\" : 1}");
        recieveLatch.await();
        ws.disconnect();

        return results;
    }

    public static void main(String[] args) throws Exception {
        Main client = new Main();
        for (String res : client.transcribe("test2.wav")) {
            System.out.println(res);
        }
    }
}
