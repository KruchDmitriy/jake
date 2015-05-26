/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author dmitry
 */
public class ClientListener implements Runnable {
    private int port = 1234;
    public static int ClientsCount;
    public List<DataManager> dms;

    @SuppressWarnings("CallToThreadStartDuringObjectConstruction")
    public ClientListener() {
        ClientsCount = 0;
        dms = new ArrayList<DataManager>();
        new Thread(this).start();
    }

    /**
     *
     * @return
     */
    public int getNumClients() {
        return ClientsCount;
    }

    public void run() {
        try {
            // Создаем слушатель
            ServerSocket socketListener;
            socketListener = new ServerSocket(port);

            while (true) {
                Socket client = null;
                while (client == null) {
                    client = socketListener.accept();
                }
                InputStream sin = client.getInputStream();
                OutputStream sout = client.getOutputStream();
                DataInputStream in = new DataInputStream(sin);
                DataOutputStream out = new DataOutputStream(sout);
                dms.add(new DataManager(in,out));
                ClientsCount++;
            }
        } catch (SocketException e) {
            System.err.println("Socket exception");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("I/O exception");
            e.printStackTrace();
        }
    }

}
