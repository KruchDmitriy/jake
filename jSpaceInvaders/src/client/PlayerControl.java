/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import com.jme3.math.FastMath;
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
    public boolean up, down, left, right;
    // speed of the player
    // lastRotation of the player
    private float lastRotation;

    public PlayerControl(DataManager dm) {
        this.dm = dm;
    }

    @Override
    protected void controlUpdate(float tpf) {
        // move the player in a certain direction
        // if he is not out of the screen
        if (up) {
            dm.SendMessage(Long.valueOf((Integer)spatial.getUserData("objid")),
                    DataManager.MessageCode.PlayerControlUpdate.value(),
                    "up");
            spatial.rotate(0, 0, -lastRotation + FastMath.PI / 2);
            lastRotation = FastMath.PI / 2;
        }
        else if (down) {
            dm.SendMessage(Long.valueOf((Integer)spatial.getUserData("objid")),
                    DataManager.MessageCode.PlayerControlUpdate.value(),
                    "down");
            spatial.rotate(0, 0, -lastRotation + FastMath.PI * 1.5f);
            lastRotation = FastMath.PI * 1.5f;
        }
        else if (left) {
            dm.SendMessage(Long.valueOf((Integer)spatial.getUserData("objid")),
                    DataManager.MessageCode.PlayerControlUpdate.value(),
                    "left");
            spatial.rotate(0, 0, -lastRotation + FastMath.PI);
            lastRotation = FastMath.PI;
        }
        else if (right) {
            dm.SendMessage(Long.valueOf((Integer)spatial.getUserData("objid")),
                    DataManager.MessageCode.PlayerControlUpdate.value(),
                    "right");
            spatial.rotate(0, 0, -lastRotation);
            lastRotation = 0;
        }
        
        String msg = "";
        
        msg = dm.FindRecord(Long.valueOf((Integer)spatial.getUserData("objid")),
                 DataManager.MessageCode.PlayerControlUpdate.value());
        if (msg.equals(""))
            return;

        String[] split = msg.split(" ");
        float x = Float.parseFloat(split[0]);
        float y = Float.parseFloat(split[1]);
        float z = Float.parseFloat(split[2]);
        spatial.setLocalTranslation(x,y,z);
    }

    public void applyGravity(Vector3f gravity) {
        
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
