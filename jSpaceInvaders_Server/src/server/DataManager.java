/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static server.ServerMain.out;
import static server.ServerMain.outmutex;

/**
 *
 * @author anton
 */

public class DataManager implements Runnable {
    /**
     *
     */
    public enum MessageCode 
    {
        // 00001-00999 - Default
        // 01000-01999 - Player
        // 02000-02999 - Bullet
        // 03000-03999 - Seeker
        // 04000-04999 - Wanderer
        // 05000-05999 - Blackhole
        
        
        SpawnFunction(00011),
        
        CreateSeeker(00101),
        
        PlayerControlUpdate(01000),
        
        SeekerControlUpdate(03000),
        SeekerControlActive(03001);
        
        private int value;
        
        private MessageCode(int value) {
            this.value = value;
        }
        
        static MessageCode fromValue(int value) {
            for (MessageCode my: MessageCode.values()) {
                if (my.value == value) {
                    return my;
                }
            }
            return null;
        }
        
        int value() {
            return value;
        }
    }
    
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
    
    @SuppressWarnings("CallToThreadStartDuringObjectConstruction")
    public DataManager(DataInputStream _in) {
        in = _in;
        data = new ArrayList<Record>();
        lostData = 0;
        mutex = new Object();
        new Thread(this).start();
    };
    
    public void run() {
        while (true) {
                boolean find = true;
            
                try {
                    if (in.readLong() != 0) { lostData++; find = false;};
                    if (find){
                        long o = in.readLong();
                        long m = in.readLong();
                        String mes = in.readUTF();
                        synchronized (mutex) {
                            Record temp = new Record (o,m,mes);
                            data.add(temp);
                            mutex.notify();
                        }
                    }
                } catch (IOException ex) {
                    
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
    
    public List<String> FindAllRecords(long o, long m) {
        List<String> result = new ArrayList<String>();
        synchronized (mutex) {
            for (int i = 0; i < data.size(); i++)
            {
                Record temp = data.get(i);
                if (temp.owner == o && temp.msgid == m)
                {
                    String msg = temp.message;
                    data.remove(i);
                    result.add(msg);
                }
            }
            mutex.notify();
            return result;
        }
    }
    
    public void SendMessage(long owner, long msgid, String message)
    {
        synchronized (outmutex) {
                try {
                    out.writeLong(0);
                    out.writeLong(owner);
                    out.writeLong(msgid);
                    out.writeUTF(message);

                } catch (IOException ex) {
                    
                }
                outmutex.notify();
            }
    }
    
}
