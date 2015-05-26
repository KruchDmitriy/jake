/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

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
    private int screenWidth, screenHeight;

    private float speed = 1100f;
    public Vector3f direction;
    private float rotation;

    public BulletControl(DataManager dm, Vector3f direction,
            int screenWidth, int screenHeight) {
        this.dm = dm;
        this.direction = direction;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    @Override
    protected void controlUpdate(float tpf) {
        // movement
        
        long id = spatial.getUserData("objid");
        
        spatial.move(direction.mult(speed * tpf));

        // roration
        float actualRotation = ServerMain.getAngleFromVector(direction);
        float r = 0;
        if (actualRotation != rotation) {
            spatial.rotate(0, 0, actualRotation - rotation);
            r =  actualRotation - rotation;
            rotation = actualRotation;
        }
        
        String msg = String.valueOf(spatial.getLocalTranslation().x) + " " +
                     String.valueOf(spatial.getLocalTranslation().y) + " " +
                     String.valueOf(spatial.getLocalTranslation().z) + " " +
                     String.valueOf(r);
        
        dm.SendMessage(id,
                DataManager.MessageCode.BulletControlUpdate.value(),
                msg);
        // check boundaries
        Vector3f loc = spatial.getLocalTranslation();
        if (loc.x > screenWidth ||
            loc.y > screenHeight ||
            loc.x < 0 ||
            loc.y < 0) {
            dm.SendMessage(
                    id,
                    DataManager.MessageCode.BulletDie.value(),
                    "die");
            spatial.removeFromParent();
        }
    }

    public void applyGravity(Vector3f gravity) {
        direction.addLocal(gravity);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {}
}
