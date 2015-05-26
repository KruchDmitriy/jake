/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import java.util.List;

/**
 *
 * @author dmitry
 */
public class SeekerControl extends AbstractControl {
    private Spatial player;
    private Vector3f velocity;
    private long spawnTime;

    private static List<DataManager> odms;
    private static List<DataManager> pdms;

    public SeekerControl(Spatial player,
                         List<DataManager> pdms,
                         List<DataManager> odms) {
        this.player = player;
        this.odms = odms;
        this.pdms = pdms;
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

            for (int i = 0; i < pdms.size(); i++) {
                DataManager dm = pdms.get(i);
                dm.SendMessage((Long)spatial.getUserData("objid"),
                    DataManager.MessageCode.SeekerControlUpdate.value(),msg);
            }
            for (int i = 0; i < odms.size(); i++) {
                DataManager dm = odms.get(i);
                dm.SendMessage((Long)spatial.getUserData("objid"),
                    DataManager.MessageCode.SeekerControlUpdate.value(),msg);
            }
        } else {
            // handle the "active" status
            long dif = System.currentTimeMillis() - spawnTime;
            if (dif >= 1000f) {
                spatial.setUserData("active", true);

                for (int i = 0; i < pdms.size(); i++) {
                    DataManager dm = pdms.get(i);
                    dm.SendMessage((Long)spatial.getUserData("objid"),
                        DataManager.MessageCode.SeekerControlActive.value(),
                        "active");
                }
                for (int i = 0; i < odms.size(); i++) {
                    DataManager dm = odms.get(i);
                    dm.SendMessage((Long)spatial.getUserData("objid"),
                        DataManager.MessageCode.SeekerControlActive.value(),
                        "active");
                }
            }

        }
    }

    public void applyGravity(Vector3f gravity) {
        velocity.addLocal(gravity);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {}
}
