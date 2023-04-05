package pl.projekt.bsk.connection;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

import lombok.Getter;
public class Sender implements Runnable {
    private Socket socket;
    private String ip;
    private int port;
    private InputStream in;
    private OutputStream out;

    @Getter
    private boolean connected = false;


    public Sender(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void startConnection() {
        try {
            socket = new Socket(ip, port);
            in = socket.getInputStream();
            out = socket.getOutputStream();

            byte[] connectionCheck = new byte[1024];  // TODO: change this value to constant (create file with CONST values)
            in.read(connectionCheck, 0, 9);

            if(!(new String(connectionCheck, 0, 9).equals("connected"))) {
                throw new Exception("Connection failed");
            }
            connected = true;
            System.out.println("Connected to: " + ip + ":" + port);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendFile(File file) throws Exception {
        int bytes = 0;

        try {
            FileInputStream fileInputStream = new FileInputStream(file);

            long fileSize = file.length();
            byte[] header = ByteBuffer.allocate(Long.BYTES).putLong(fileSize).array();
            out.write(header);


            byte[] buffer = new byte[1024];  // TODO: change this value to constant (create file with CONST values)
            while ((bytes = fileInputStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytes);
//                out.flush();
            }
            

            fileInputStream.close();
            System.out.println("Koniec wysyłania");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void run() {
        startConnection();
    }
}
