/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

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
    DataManager dm;
    private long spawnTime;
    private int hitpoints;

    public BlackHoleControl(DataManager dm) {
        this.dm =dm;
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

            ColorRGBA color = new ColorRGBA(1f, 1f, 1f, dif/1000f);
            Node spatialNode = (Node)spatial;
            Picture pic = (Picture) spatialNode.getChild("Black Hole");
            pic.getMaterial().setColor("Color", color);
            
            String msg = "";
            msg = dm.FindRecord((Integer)spatial.getUserData("objid"),
                     DataManager.MessageCode.WandererControlActive.value());
            if (msg.equals(""))
                return;   
            else
                spatial.setUserData("active", true);
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
