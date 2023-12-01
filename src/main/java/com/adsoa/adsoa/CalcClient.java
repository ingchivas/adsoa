package com.adsoa.adsoa;
import java.net.*;
import java.io.*;

public class CalcClient {
    private Socket clientSocket;
    private OutputStream out;
    private BufferedReader in;

    public void startConnection(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = clientSocket.getOutputStream();
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

    }

    public String sendMessage(byte[] msg) throws IOException {
        out.write(msg);
        out.flush();
        String resp = in.readLine();
        return resp;
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }
}