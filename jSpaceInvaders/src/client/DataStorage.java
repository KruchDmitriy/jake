/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author anton
 */

public class DataStorage implements Runnable {
    public class Record{
        public long owner;
        public long msgid;
        public String message;
        public Record(long o, long m, String mes) {
            owner = o;
            msgid = m;
            message = mes;
        };
    }
    
    private DataInputStream in;
    private List<Record> data;
    public long lostData;
    private Object mutex;
    
    public DataStorage(DataInputStream _in) {
        in = _in;
        data = new ArrayList<Record>();
        lostData = 0;
        mutex = new Object();
        new Thread(this).start();
    };
    
    public void run() {
        while (true) {
            synchronized (mutex) {
                try {
                    while (in.readLong() != 0) { lostData++; };
                    long o = in.readLong();
                    long m = in.readLong();
                    String mes = in.readUTF();
                    Record temp = new Record (o,m,mes);
                    data.add(temp);

                } catch (IOException ex) {
                    Logger.getLogger(DataStorage.class.getName()).log(Level.SEVERE, null, ex);
                }
                mutex.notify();
            }
        }
    };
    public String FindRecord(long o, long m) {
        synchronized (mutex) {
            for (int i = 0; i < data.size(); i++)
            {
                Record temp = data.get(i);
                if (temp.owner == o && temp.msgid == m)
                {
                    String msg = temp.message;
                    data.remove(i);
                    mutex.notify();
                    return msg;
                }
            }
            mutex.notify();
            return "";
        }
    };
    
}
