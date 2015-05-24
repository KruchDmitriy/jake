/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;
import com.jme3.ui.Picture;
import java.util.Random;

/**
 *
 * @author dmitry
 */
public class WandererControl extends AbstractControl {
    private DataManager dm;
    private long spawnTime;

    public WandererControl(DataManager dm) {
        this.dm = dm;
        spawnTime = System.currentTimeMillis();
    }

    @Override
    protected void controlUpdate(float tpf) {
        if ((Boolean) spatial.getUserData("active")) {
            String msg = "";
            
            msg = dm.FindRecord((Integer)spatial.getUserData("objid"),
                     DataManager.MessageCode.WandererControlUpdate.value());
            if (msg.equals(""))
                return;
            
            String[] split = msg.split(" ");
            float x = Float.parseFloat(split[0]);
            float y = Float.parseFloat(split[1]);
            float z = Float.parseFloat(split[2]);
            spatial.setLocalTranslation(x,y,z);
            
            // rotate the wanderer
            spatial.rotate(0f, 0f, tpf * 2f);
        } else {
            // handle the "active"-status
            long dif = System.currentTimeMillis() - spawnTime;
            
            ColorRGBA color = new ColorRGBA(1f, 1f, 1f, dif / 1000f);
            Node spatialNode = (Node)spatial;
            Picture pic = (Picture)spatialNode.getChild("Wanderer");
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

    public void applyGravity(Vector3f gravity) {
        
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {}
}
