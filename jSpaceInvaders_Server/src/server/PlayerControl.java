/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author dmitry
 */
public class PlayerControl extends AbstractControl {
    private DataManager dm;
    private int screenWidth, screenHeight;

    public boolean up, down, left, right;
    // speed of the player
    private float speed = 800f;
    // lastRotation of the player
    private float lastRotation;

    public PlayerControl(DataManager dm, int width, int height) {
        this.dm = dm;
        this.screenWidth  = width;
        this.screenHeight = height;
    }

    @Override
    protected void controlUpdate(float tpf) {
        String msg = "";
        msg = dm.FindRecord((Long)spatial.getUserData("objid"),
                DataManager.MessageCode.PlayerControlUpdate.value());
        if (msg.equals(""))
            return;
                
        if (msg.equals("up")) {
            if (spatial.getLocalTranslation().y <
                    screenHeight - (Float)spatial.getUserData("radius")) {
                spatial.move(0, tpf * speed, 0);
            }
            spatial.rotate(0, 0, -lastRotation + FastMath.PI / 2);
            lastRotation = FastMath.PI / 2;
        }
        else if (msg.equals("down")) {
            if (spatial.getLocalTranslation().y >
                    (Float)spatial.getUserData("radius")) {
                spatial.move(0, tpf * (-speed), 0);
            }
            spatial.rotate(0, 0, -lastRotation + FastMath.PI * 1.5f);
            lastRotation = FastMath.PI * 1.5f;
        }
        else if (msg.equals("left")) {
            if (spatial.getLocalTranslation().x >
                    (Float)spatial.getUserData("radius")) {
                spatial.move(tpf * (-speed), 0, 0);
            }
            spatial.rotate(0, 0, -lastRotation + FastMath.PI);
            lastRotation = FastMath.PI;
        }
        else if (msg.equals("tight")) {
            if (spatial.getLocalTranslation().x <
                    screenWidth - (Float)spatial.getUserData("radius")) {
                spatial.move(tpf * speed, 0, 0);
            }
            spatial.rotate(0, 0, -lastRotation);
            lastRotation = 0;
        }
        
        msg = String.valueOf(spatial.getLocalTranslation().x) + " " +
              String.valueOf(spatial.getLocalTranslation().y) + " " +
              String.valueOf(spatial.getLocalTranslation().z);
        dm.SendMessage(
                (Long)spatial.getUserData("objid"),
                DataManager.MessageCode.PlayerControlUpdate.value(),
                msg);
    }

    public void applyGravity(Vector3f gravity) {
        spatial.move(gravity);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {}

    // reset the moving values (i.e. for spawning)
    public void reset() {
        up = false;
        down = false;
        left = false;
        right = false;
    }
}
