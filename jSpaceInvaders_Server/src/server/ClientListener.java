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

/**
 *
 * @author dmitry
 */
public class ClientListener implements Runnable {
    public static long ClientsCount;

    public ClientListener() {
        ClientsCount = 0;
    }

    public void run() {
        try {
            // Создаем слушатель
            ServerSocket socketListener;
            socketListener = new ServerSocket(1234);

            while (true) {
                Socket client = null;
                while (client == null) {
                    client = socketListener.accept();
                    ClientsCount++;
                }
                InputStream sin = client.getInputStream();
                OutputStream sout = client.getOutputStream();
                DataInputStream in = new DataInputStream(sin);
                DataOutputStream out = new DataOutputStream(sout);
                DataManager dm = new DataManager(in,out);
                // new ClientThread(client); Создаем новый поток, которому передаем сокет
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
