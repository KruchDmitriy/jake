/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import java.util.List;
import java.util.Random;

/**
 *
 * @author dmitry
 */
public class WandererControl extends AbstractControl {
    private static List<DataManager> odms;
    private static List<DataManager> pdms;

    private int screenWidth, screenHeight;

    private Vector3f velocity;
    private float directionAngle;
    private long spawnTime;

    public WandererControl(List<DataManager> pdms,
            List<DataManager> odms,
            int screenWidth, int screenHeight) {
        this.odms = odms;
        this.pdms = pdms;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        velocity = new Vector3f();
        directionAngle = new Random().nextFloat() * FastMath.PI * 2f;
        spawnTime = System.currentTimeMillis();
    }

    @Override
    protected void controlUpdate(float tpf) {
        if ((Boolean) spatial.getUserData("active")) {
            // make the wanderer bounce off the screen borders
            Vector3f loc = spatial.getLocalTranslation();

            if (FastMath.abs(loc.x - screenWidth / 2f) > screenWidth / 2f) {
                velocity.x = -velocity.x;
                directionAngle += FastMath.PI;
            }
            if (FastMath.abs(loc.y - screenHeight/2f) > screenHeight / 2f) {
                velocity.y = -velocity.y;
                directionAngle += FastMath.PI;
            }
            if (directionAngle > FastMath.TWO_PI) {
                directionAngle -= FastMath.TWO_PI;
            }

            directionAngle += (new Random().nextFloat() * 20f - 10f) * tpf;
            Vector3f directionVector = ServerMain.getVectorFromAngle(
                    directionAngle);
            directionVector.multLocal(1000f);
            velocity.addLocal(directionVector);

            // decrease the velocity a bit and move the wanderer
            velocity.multLocal(0.8f);
            spatial.move(velocity.mult(tpf * 0.1f));

            // rotate the wanderer
            spatial.rotate(0f, 0f, tpf * 2f);

            // Send data to client
            String msg;
            msg = String.valueOf(spatial.getLocalTranslation().x) + " " +
                  String.valueOf(spatial.getLocalTranslation().y) + " " +
                  String.valueOf(spatial.getLocalTranslation().z) + " ";

            for (int i = 0; i < pdms.size(); i++) {
                DataManager dm = pdms.get(i);
                dm.SendMessage((Long)spatial.getUserData("objid"),
                    DataManager.MessageCode.WandererControlUpdate.value(),msg);
            }
            for (int i = 0; i < odms.size(); i++) {
                DataManager dm = odms.get(i);
                dm.SendMessage((Long)spatial.getUserData("objid"),
                    DataManager.MessageCode.WandererControlUpdate.value(),msg);
            }

        } else {
            // handle the "active"-status
            long dif = System.currentTimeMillis() - spawnTime;
            if (dif >= 1000f) {
                spatial.setUserData("active", true);
                for (int i = 0; i < pdms.size(); i++) {
                    DataManager dm = pdms.get(i);
                    dm.SendMessage((Long)spatial.getUserData("objid"),
                        DataManager.MessageCode.WandererControlActive.value(),
                        "active");
                }
                for (int i = 0; i < odms.size(); i++) {
                    DataManager dm = odms.get(i);
                    dm.SendMessage((Long)spatial.getUserData("objid"),
                        DataManager.MessageCode.WandererControlActive.value(),
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
