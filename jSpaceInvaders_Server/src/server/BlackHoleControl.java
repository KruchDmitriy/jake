/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import java.util.List;

/**
 *
 * @author dmitry
 */
public class BlackHoleControl extends AbstractControl {
    private static List<DataManager> odms;
    private static List<DataManager> pdms;
    private long spawnTime;
    private int hitpoints;

    public BlackHoleControl(List<DataManager> pdms,
                            List<DataManager> odms) {
        this.pdms = pdms;
        this.odms = odms;
        spawnTime = System.currentTimeMillis();
        hitpoints = 10;
    }

    @Override
    protected void controlUpdate(float tpf) {
        if ((Boolean) spatial.getUserData("active")) {
            // use this spot later
        } else {
            // handle the "active" status
            long dif = System.currentTimeMillis() - spawnTime;
            if (dif >= 1000f) {
                spatial.setUserData("active", true);

                for (int i = 0; i < pdms.size(); i++) {
                    DataManager dm = pdms.get(i);
                    dm.SendMessage((Long)spatial.getUserData("objid"),
                        DataManager.MessageCode.BlackHoleControlActive.value(),
                        "active");
                }
                for (int i = 0; i < odms.size(); i++) {
                    DataManager dm = odms.get(i);
                    dm.SendMessage((Long)spatial.getUserData("objid"),
                        DataManager.MessageCode.BlackHoleControlActive.value(),
                        "active");
                }
            }
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {}

    public void wasShot() {
        hitpoints--;
    }

    public boolean isDead() {
        return hitpoints <= 0;
    }
}
