/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

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
    private int screenWidth, screenHeight;

    public boolean up, down, left, right;
    // speed of the player
    private float speed = 800f;
    // lastRotation of the player
    private float lastRotation;

    public PlayerControl(int width, int height) {
        this.screenWidth  = width;
        this.screenHeight = height;
    }

    @Override
    protected void controlUpdate(float tpf) {
        // move the player in a certain direction
        // if he is not out of the screen
        if (up) {
            if (spatial.getLocalTranslation().y <
                    screenHeight - (Float)spatial.getUserData("radius")) {
                spatial.move(0, tpf * speed, 0);
            }
            spatial.rotate(0, 0, -lastRotation + FastMath.PI / 2);
            lastRotation = FastMath.PI / 2;
        }
        else if (down) {
            if (spatial.getLocalTranslation().y >
                    (Float)spatial.getUserData("radius")) {
                spatial.move(0, tpf * (-speed), 0);
            }
            spatial.rotate(0, 0, -lastRotation + FastMath.PI * 1.5f);
            lastRotation = FastMath.PI * 1.5f;
        }
        else if (left) {
            if (spatial.getLocalTranslation().x >
                    (Float)spatial.getUserData("radius")) {
                spatial.move(tpf * (-speed), 0, 0);
            }
            spatial.rotate(0, 0, -lastRotation + FastMath.PI);
            lastRotation = FastMath.PI;
        }
        else if (right) {
            if (spatial.getLocalTranslation().x <
                    screenWidth - (Float)spatial.getUserData("radius")) {
                spatial.move(tpf * speed, 0, 0);
            }
            spatial.rotate(0, 0, -lastRotation);
            lastRotation = 0;
        }
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
