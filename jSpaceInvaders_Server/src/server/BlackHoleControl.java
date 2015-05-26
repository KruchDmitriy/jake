/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;
import com.jme3.ui.Picture;

/**
 *
 * @author dmitry
 */
public class BlackHoleControl extends AbstractControl {
    private DataManager dm;
    private long spawnTime;
    private int hitpoints;

    public BlackHoleControl(DataManager dm) {
        this.dm = dm;
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
                dm.SendMessage((Long)spatial.getUserData("objid"), 
                    DataManager.MessageCode.BlackHoleControlActive.value(), "active");
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
