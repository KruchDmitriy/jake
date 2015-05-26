/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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


        SimpleInitApp(00010),
        SpawnFunction(00011),
        HandleCollision(00012),
        OnAnalog(00013),

        WhoAreYou(00020),

        Observer(00030),

        WidthAndHeight(00051),
        PlayerRadius(00052),
        PlayerID(00053),

        CreateSeeker(00101),
        CreateWanderer(00102),
        CreateBlackHole(00103),
        CreateBullet(00103),

        PlayerControlUpdate(01000),
        PlayerDie(01001),

        BulletControlUpdate(02000),
        BulletDie(02001),
        EnemyDie(02700),

        SeekerControlUpdate(03000),
        SeekerControlActive(03001),

        WandererControlUpdate(04000),
        WandererControlActive(04001),

        BlackHoleControlActive(05000),
        BlackHoleDie(05001);

        private long value;

        private MessageCode(long value) {
            this.value = value;
        }

        static MessageCode fromValue(long value) {
            for (MessageCode my: MessageCode.values()) {
                if (my.value == value) {
                    return my;
                }
            }
            return null;
        }

        long value() {
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
    private DataOutputStream out;
    private List<Record> data;
    public long lostData;
    private Object inmutex;
    private Object outmutex;

    @SuppressWarnings("CallToThreadStartDuringObjectConstruction")
    public DataManager(DataInputStream _in, DataOutputStream _out) {
        in = _in;
        out = _out;
        data = new ArrayList<Record>();
        lostData = 0;
        inmutex = new Object();
        outmutex = new Object();
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
                        synchronized (inmutex) {
                            Record temp = new Record (o,m,mes);
                            data.add(temp);
                            inmutex.notify();
                        }
                    }
                } catch (IOException ex) {

                }
        }
    };
    public String FindRecord(long o, long m) {
        synchronized (inmutex) {
            for (int i = 0; i < data.size(); i++)
            {
                Record temp = data.get(i);
                if (temp.owner == o && temp.msgid == m)
                {
                    String msg = temp.message;
                    data.remove(i);
                    inmutex.notify();
                    return msg;
                }
            }
            inmutex.notify();
            return "";
        }
    };

    public List<String> FindAllRecords(long o, long m) {
        List<String> result = new ArrayList<String>();
        synchronized (inmutex) {
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
            inmutex.notify();
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
