/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

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
    private DataManager dm;
    private Spatial player;
    private Vector3f velocity;
    private long spawnTime;

    public SeekerControl(Spatial player, DataManager dm) {
        this.player = player;
        this.dm = dm;
        velocity = new Vector3f(0f, 0f, 0f);
        spawnTime = System.currentTimeMillis();
    }

    @Override
    protected void controlUpdate(float tpf) {
        if ((Boolean) spatial.getUserData("active")) {
            // translate the seeker
            Vector3f playerDirection;
            playerDirection = (player.getLocalTranslation()).subtract(
                spatial.getLocalTranslation());
            playerDirection.normalizeLocal();
            playerDirection.multLocal(1000f);
            velocity.addLocal(playerDirection);
            velocity.multLocal(0.8f);
            spatial.move(velocity.mult(tpf * 0.1f));

            Vector3f vel = velocity.normalize();
            // rotate the seeker
            if (velocity != Vector3f.ZERO) {
                spatial.rotateUpTo(vel);
            }
            
           
            // to send
            String msg;
            msg = String.valueOf(spatial.getLocalTranslation().x) + " " +
                  String.valueOf(spatial.getLocalTranslation().y) + " " +
                  String.valueOf(spatial.getLocalTranslation().z) + " " +
                  String.valueOf(vel.x) + " " +
                  String.valueOf(vel.y) + " " +
                  String.valueOf(vel.z);
            dm.SendMessage((Integer)spatial.getUserData("objid"), 
                    DataManager.MessageCode.SeekerControlUpdate.value(),msg);
        } else {
            // handle the "active" status
            long dif = System.currentTimeMillis() - spawnTime;
            if (dif >= 1000f) {
                spatial.setUserData("active", true);
            }
            dm.SendMessage((Integer)spatial.getUserData("objid"), 
                    DataManager.MessageCode.SeekerControlActive.value(), "active");
        }
    }

    public void applyGravity(Vector3f gravity) {
        velocity.addLocal(gravity);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {}
}
