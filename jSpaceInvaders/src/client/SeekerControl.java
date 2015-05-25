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
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.ui.Picture;

/**
 *
 * @author dmitry
 */
public class SeekerControl extends AbstractControl {
    private long spawnTime;
    private DataManager dm;

    public SeekerControl(DataManager dm) {
        spawnTime = System.currentTimeMillis();
        this.dm = dm;
    }

    @Override
    protected void controlUpdate(float tpf) {
        if ((Boolean) spatial.getUserData("active")) {
            // translate the seeker
            String msg = "";
            
            msg = dm.FindRecord(Long.valueOf((Integer)spatial.getUserData("objid")),
                     DataManager.MessageCode.SeekerControlUpdate.value());
            if (msg.equals(""))
                return;
            
            String[] split = msg.split(" ");
            float x = Float.parseFloat(split[0]);
            float y = Float.parseFloat(split[1]);
            float z = Float.parseFloat(split[2]);
            spatial.setLocalTranslation(x,y,z);
            x = Float.parseFloat(split[3]);
            y = Float.parseFloat(split[4]);
            z = Float.parseFloat(split[5]);
      
            Vector3f velocity = new Vector3f(x,y,z);
            //
            
            
            // rotate the seeker
            if (velocity != Vector3f.ZERO) {
                spatial.rotateUpTo(velocity.normalize());
            }
        } else {
            // handle the "active" status
            long dif = System.currentTimeMillis() - spawnTime;

            ColorRGBA color = new ColorRGBA(1f, 1f, 1f, dif / 1000f);
            Node spatialNode = (Node)spatial;
            Picture pic = (Picture) spatialNode.getChild("Seeker");
            pic.getMaterial().setColor("Color", color);
            
            String msg = "";
            msg = dm.FindRecord(Long.valueOf((Integer)spatial.getUserData("objid")),
                     DataManager.MessageCode.SeekerControlActive.value());
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
