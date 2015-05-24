/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author dmitry
 */
public class BulletControl extends AbstractControl {
    DataManager dm;

    public Vector3f direction;


    public BulletControl(DataManager dm, Vector3f direction,
            int screenWidth, int screenHeight) {
        this.dm = dm;
        this.direction = direction;
    }

    @Override
    protected void controlUpdate(float tpf) {
        // movement
        String msg = "";
        msg = dm.FindRecord((Integer)spatial.getUserData("objid"),
                 DataManager.MessageCode.BulletControlUpdate.value());
        if (msg.equals(""))
            return;   
        
        String[] split = msg.split(" ");
        float x = Float.parseFloat(split[0]);
        float y = Float.parseFloat(split[1]);
        float z = Float.parseFloat(split[2]);
        spatial.setLocalTranslation(x,y,z);
        
        float r = Float.parseFloat(split[3]);
        spatial.rotate(0, 0, r);
        
        msg = dm.FindRecord((Integer)spatial.getUserData("objid"),
                 DataManager.MessageCode.BulletDie.value());
        if (msg.equals(""))
            return;
        else
            spatial.removeFromParent();
    }

    public void applyGravity(Vector3f gravity) {
        
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {}
}
