package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.renderer.RenderManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.scene.Node;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;

public class MonkeyBlasterMain extends SimpleApplication 
    implements ActionListener,
               AnalogListener {
    private Spatial player;
    private Node bulletNode;
    private long bulletCooldown = System.currentTimeMillis();

    public static void main(String[] args) {
        MonkeyBlasterMain app = new MonkeyBlasterMain();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // setup camera for 2D games
        cam.setParallelProjection(true);
        cam.setLocation(new Vector3f(0.0f, 0.0f, 0.5f));
        getFlyByCamera().setEnabled(false);
        
        // turn off stats view
        setDisplayStatView(false);
        setDisplayFps(false);
        
        inputManager.addMapping("left", new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("right", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("up", new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("down", new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("return", new KeyTrigger(KeyInput.KEY_RETURN));
        
        inputManager.addListener(this, "left");
        inputManager.addListener(this, "right");
        inputManager.addListener(this, "up");
        inputManager.addListener(this, "down");
        inputManager.addListener(this, "return");
        
        inputManager.addMapping("mousePick",
                new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(this, "mousePick");
        
        // setup the player
        player = getSpatial("Player");
        player.setUserData("alive", true);
        player.move(settings.getWidth()/2, settings.getHeight()/2, 0f);
        player.addControl(new PlayerControl(settings.getWidth(),
                settings.getHeight()));
        
        bulletNode = new Node("bullets");
        guiNode.attachChild(bulletNode);
        guiNode.attachChild(player);
    }

    @Override
    public void simpleUpdate(float tpf) {
    }

    @Override
    public void simpleRender(RenderManager rm) {
    }
    
    private Spatial getSpatial(String name) {
        Node node = new Node(name);
        // load picture
        Picture pic;
        pic = new Picture(name);
        Texture2D tex = (Texture2D)assetManager.loadTexture(
                "Textures/"+name+".png");
        pic.setTexture(assetManager, tex, true);
        
        // adjust picture
        float width = tex.getImage().getWidth();
        float height = tex.getImage().getHeight();
        pic.setWidth(width);
        pic.setHeight(height);
        pic.move(-width/2f, -height/2f, 0);
        
        // add a material to the picture
        Material picMat = new Material(assetManager, 
                "Common/MatDefs/Gui/Gui.j3md");
        picMat.getAdditionalRenderState().setBlendMode(BlendMode.AlphaAdditive);
        node.setMaterial(picMat);
        
        // set the radius of the spatial
        // using width only as a simple approximation
        node.setUserData("radius", width/2);
        
        // attach the picture to the node and return it
        node.attachChild(pic);
        return node;
    }
    
    public void onAction(String name, boolean isPressed, float tpf) {
        if ((Boolean) player.getUserData("alive")) {
            if (name.equals("up")) {
                player.getControl(PlayerControl.class).up = isPressed;
            } else if (name.equals("down")) {
                player.getControl(PlayerControl.class).down = isPressed;
            } else if (name.equals("left")) {
                player.getControl(PlayerControl.class).left = isPressed;
            } else if (name.equals("right")) {
                player.getControl(PlayerControl.class).right = isPressed;
            }
        }
    }
    
    private Vector3f getAimDirection() {
        Vector2f mouse = inputManager.getCursorPosition();
        Vector3f playerPos = player.getLocalTranslation();
        Vector3f diff = new Vector3f(mouse.x - playerPos.x,
                                    mouse.y - playerPos.y, 0f);
        return diff.normalizeLocal();
    }
    
    public void onAnalog(String name, float value, float tpf) {
        if ((Boolean) player.getUserData("alive")) {
            if (name.equals("mousePick")) {
                if (System.currentTimeMillis() - bulletCooldown > 83) {
                    bulletCooldown = System.currentTimeMillis();
                    
                    Vector3f aim = getAimDirection();
                    Vector3f offset = new Vector3f(aim.y/3, -aim.x/3, 0f);
                    
                    Spatial bullet = getSpatial("Bullet");
                    Vector3f finalOffset = aim.add(offset).mult(30);
                    Vector3f trans = 
                            player.getLocalTranslation().add(finalOffset);
                    bullet.setLocalTranslation(trans);
                    bullet.addControl(
                            new BulletControl(aim, 
                                              settings.getWidth(),
                                              settings.getHeight()));
                    bulletNode.attachChild(bullet);
                    
                    Spatial bullet2 = getSpatial("Bullet");
                    finalOffset = aim.add(offset.negate()).mult(30);
                    trans = player.getLocalTranslation().add(finalOffset);
                    bullet2.setLocalTranslation(trans);
                    bullet2.addControl(
                            new BulletControl(
                            aim, settings.getWidth(), settings.getHeight()));
                    bulletNode.attachChild(bullet2);
                }
            }
        }
    }
    
    public static float getAngleFromVector(Vector3f vec) {
        Vector2f vec2 = new Vector2f(vec.x, vec.y);
        return vec2.getAngle();
    }
    
    public static Vector3f getVectorFromAngle(float angle) {
        return new Vector3f(FastMath.cos(angle), FastMath.sin(angle), 0f);
    }
}
