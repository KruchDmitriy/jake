package client;

import com.jme3.app.SimpleApplication;
import com.jme3.cursors.plugins.JmeCursor;
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
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.scene.Spatial;
import com.jme3.scene.Node;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ClientMain extends SimpleApplication
    implements ActionListener,
               AnalogListener {
    
    public static Socket socket;
    public static InputStream sin;
    public static OutputStream sout;
    public static DataInputStream in;
    public static DataOutputStream out;
    
    public static DataManager dm;
    
    private Spatial player;

    private long bulletCooldown;
    private long enemySpawnCooldown;
    private float enemySpawnChance = 80;
    private long spawnCooldownBlackHole;

    private boolean gameOver = false;

    private Node bulletNode;
    private Node enemyNode;
    private Node blackHoleNode;

    private Sound sound;

    private HUD hud;

    @SuppressWarnings("empty-statement")
    public static void main(String[] args) {
        int port = 6666;
        String address = "127.0.0.1";
        try {
            InetAddress ipAddress = InetAddress.getByName(address);
            socket = new Socket(ipAddress, port);
            sin = socket.getInputStream();
            sout = socket.getOutputStream();
            in = new DataInputStream(sin);
            out = new DataOutputStream(sout);
        } catch (Exception x) {};
        dm = new DataManager(in,out);
        ClientMain app = new ClientMain();
        app.setPauseOnLostFocus(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        
        dm.SendMessage(
                DataManager.MessageCode.SimpleInitApp.value(),
                DataManager.MessageCode.WidthAndHeight.value(),
                String.valueOf(settings.getWidth()) + " " + String.valueOf(settings.getHeight()));
        // setup camera for 2D games
        cam.setParallelProjection(true);
        cam.setLocation(new Vector3f(0.0f, 0.0f, 0.5f));
        getFlyByCamera().setEnabled(false);

        // turn off stats view
        setDisplayStatView(false);
        setDisplayFps(false);

        bulletCooldown = System.currentTimeMillis();

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
        player.addControl(new PlayerControl(dm));
        
        dm.SendMessage(
                DataManager.MessageCode.SimpleInitApp.value(),
                DataManager.MessageCode.PlayerRadius.value(),
                String.valueOf(player.getUserData("radius")));
        
        String msg = "";
        boolean notfind = true;
        while (notfind) {
             msg = dm.FindRecord(
                     DataManager.MessageCode.SimpleInitApp.value(),
                     DataManager.MessageCode.PlayerID.value());
             if (!msg.equals(""))
                 notfind = false;
        };
        
        player.setUserData("objid", Long.parseLong(msg));
        

        bulletNode = new Node("bullets");
        enemyNode = new Node("enemies");
        blackHoleNode = new Node("black holes");

        guiNode.attachChild(enemyNode);
        guiNode.attachChild(blackHoleNode);
        guiNode.attachChild(bulletNode);
        guiNode.attachChild(player);
        
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        BloomFilter bloom = new BloomFilter();
        bloom.setBloomIntensity(2f);
        bloom.setExposurePower(2f);
        bloom.setExposureCutOff(0f);
        bloom.setBlurScale(1.5f);
        fpp.addFilter(bloom);
        guiViewPort.addProcessor(fpp);
        guiViewPort.setClearColor(true);

        hud = new HUD(assetManager, guiNode,
                settings.getWidth(), settings.getHeight());
        hud.reset();
        inputManager.setMouseCursor(
                (JmeCursor) assetManager.loadAsset("Textures/Pointer.ico"));

        sound = new Sound(assetManager);
        //sound.startMusic();
    }

    @Override
    public void simpleUpdate(float tpf) {
        if ((Boolean) player.getUserData("alive")) {
            spawnEnemies();
            spawnBlackHoles();
            handleCollisions();
        } else if ((System.currentTimeMillis() -
                (Long)player.getUserData("dieTime")) > 4000f
                && !gameOver) {
            // spawn player
            player.setLocalTranslation(
                    settings.getWidth() / 2f,
                    settings.getHeight() / 2f,
                    0f);
            guiNode.attachChild(player);
            player.setUserData("alive", true);
        }
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
                    // send aim
                    String msg = String.valueOf(aim.x) + " " +
                                 String.valueOf(aim.y) + " " +
                                 String.valueOf(aim.z);
                    
                    dm.SendMessage(
                            DataManager.MessageCode.OnAnalog.value(),
                            DataManager.MessageCode.CreateBullet.value(), 
                            msg);
                    
                    msg = "";
                    while (msg.equals("")){
                        msg = dm.FindRecord(DataManager.MessageCode.OnAnalog.value(),
                            DataManager.MessageCode.CreateBullet.value());
                    }
                    String[] split = msg.split(" ");
                    long id = Long.parseLong(split[0]);
                    Vector3f pos = new Vector3f(Float.valueOf(split[1]),
                    Float.valueOf(split[2]),
                    Float.valueOf(split[3]));
                    
                    Spatial bullet = getSpatial("Bullet");
                    bullet.setUserData("objid", id);
                    bullet.setLocalTranslation(pos);
                    bullet.addControl(
                            new BulletControl(dm, aim,
                                              settings.getWidth(),
                                              settings.getHeight()));
                    bulletNode.attachChild(bullet);
                    
                    msg = "";
                    while (msg.equals("")){
                        msg = dm.FindRecord(DataManager.MessageCode.OnAnalog.value(),
                            DataManager.MessageCode.CreateBullet.value());
                    }
                    split = msg.split(" ");
                    id = Long.parseLong(split[0]);
                    pos = new Vector3f(Float.valueOf(split[1]),
                    Float.valueOf(split[2]),
                    Float.valueOf(split[3]));
                    
                    Spatial bullet2 = getSpatial("Bullet");
                    bullet2.setUserData("objid", id);
                    bullet2.setLocalTranslation(pos);
                    bullet2.addControl(
                            new BulletControl( dm,
                            aim, settings.getWidth(), settings.getHeight()));
                    bulletNode.attachChild(bullet2);

                    sound.shoot();
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

    private void spawnEnemies() {
        List<String> newSeekers = dm.FindAllRecords(
                DataManager.MessageCode.SpawnFunction.value(),
                DataManager.MessageCode.CreateSeeker.value());
        for (int i = 0; i < newSeekers.size(); i++) {
            createSeeker(newSeekers.get(i));
        }
        
        List<String> newWanderers = dm.FindAllRecords(
                DataManager.MessageCode.SpawnFunction.value(),
                DataManager.MessageCode.CreateWanderer.value());
        for (int i = 0; i < newWanderers.size(); i++) {
            createWanderer(newWanderers.get(i));
        }
        
    }

    private void createSeeker(String msg) {
        // Parse message
        String[] split = msg.split(" ");
        long id = Long.parseLong(split[0]);
        float x = Float.parseFloat(split[1]);
        float y = Float.parseFloat(split[2]);
        float z = Float.parseFloat(split[3]);
        
        Spatial seeker = getSpatial("Seeker");
        seeker.setLocalTranslation(new Vector3f(x,y,z));
        seeker.addControl(new SeekerControl(dm));
        seeker.setUserData("objid", id);
        seeker.setUserData("active", false);
        enemyNode.attachChild(seeker);
        sound.spawn();
    }

    private void createWanderer(String msg) {
        String[] split = msg.split(" ");
        long id = Long.parseLong(split[0]);
        float x = Float.parseFloat(split[1]);
        float y = Float.parseFloat(split[2]);
        float z = Float.parseFloat(split[3]);
        
        Spatial wanderer = getSpatial("Wanderer");
        wanderer.setLocalTranslation(new Vector3f(x,y,z));
        wanderer.addControl(new WandererControl(dm));
        wanderer.setUserData("active", false);
        wanderer.setUserData("objid", id);
        enemyNode.attachChild(wanderer);
        sound.spawn();
    }

    private void handleCollisions() {
        String pDie = dm.FindRecord(
                DataManager.MessageCode.HandleCollision.value(),
                DataManager.MessageCode.PlayerDie.value());
        if (!pDie.equals(""))
        {
            killPlayer();
            sound.explosion();
            return;
        }
        
        List<String> enemyDies = dm.FindAllRecords(
                DataManager.MessageCode.HandleCollision.value(),
                DataManager.MessageCode.EnemyDie.value());
        
        for (int i = 0; i < enemyDies.size(); i++)
        {
            int n = Integer.parseInt(enemyDies.get(i));
            if (enemyNode.getChild(n).getName().equals("Seeker")) {
                        hud.addPoints(2);
                    } else if (enemyNode.getChild(n).getName().equals(
                            "Wanderer")) {
                        hud.addPoints(1);
                    }
            sound.explosion();
            enemyNode.detachChildAt(n);
        }
        
        List<String> bulletsDies = dm.FindAllRecords(
                DataManager.MessageCode.HandleCollision.value(),
                DataManager.MessageCode.BulletDie.value());
        
        for (int i = 0; i < bulletsDies.size(); i++)
        {
            int n = Integer.parseInt(bulletsDies.get(i));
            enemyNode.detachChildAt(n);
        }
        
        List<String> blackHoleDies = dm.FindAllRecords(
                DataManager.MessageCode.HandleCollision.value(),
                DataManager.MessageCode.BlackHoleDie.value());
        
        for (int i = 0; i < blackHoleDies.size(); i++)
        {
            int n = Integer.parseInt(blackHoleDies.get(i));
            enemyNode.detachChildAt(n);
            sound.explosion();
        }
       
    }

    private void killPlayer() {
        if (!hud.removeLife()) {
            hud.endGame();
            gameOver = true;
        }
        player.removeFromParent();
        player.getControl(PlayerControl.class).reset();
        player.setUserData("alive", false);
        player.setUserData("dieTime", System.currentTimeMillis());
        enemyNode.detachAllChildren();
        blackHoleNode.detachAllChildren();
    }

    private void spawnBlackHoles() {
        List<String> newBlackHoles = dm.FindAllRecords(
                DataManager.MessageCode.SpawnFunction.value(),
                DataManager.MessageCode.CreateBlackHole.value());
        for (int i = 0; i < newBlackHoles.size(); i++) {
            createBlackHole(newBlackHoles.get(i));
        }
    }

    private void createBlackHole(String msg) {
        String[] split = msg.split(" ");
        int id = Integer.parseInt(split[0]);
        float x = Float.parseFloat(split[1]);
        float y = Float.parseFloat(split[2]);
        float z = Float.parseFloat(split[3]);
        
        
        Spatial blackHole = getSpatial("Black Hole");
        blackHole.setLocalTranslation(new Vector3f(x,y,z));
        blackHole.addControl(new BlackHoleControl(dm));
        blackHole.setUserData("active", false);
        blackHoleNode.attachChild(blackHole);
    }
}
