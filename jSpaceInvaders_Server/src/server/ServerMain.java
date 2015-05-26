package server;

import com.jme3.app.SimpleApplication;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * test
 * @author normenhansen
 */
public class ServerMain extends SimpleApplication {
    private static ClientListener clistener;
    private static int playersCount;
    private static int observersCount;

    public static List<DataManager> odms;
    public static List<DataManager> pdms;

    public static int width;
    public static int height;

    public static long ObjectsCount;

    private long enemySpawnCooldown;
    private float enemySpawnChance = 80;
    private long spawnCooldownBlackHole;

    private float seekerRadius;
    private float wandererRadius;
    private float blackHoleRadius;
    private float bulletRadius;

    private boolean gameOver = false;

    private Node playerNode;
    private Node bulletNode;
    private Node enemyNode;
    private Node blackHoleNode;

    public static void main(String[] args) {
        playersCount = 0;
        observersCount = 0;
        clistener = new ClientListener();

        odms = new ArrayList<DataManager>();
        pdms = new ArrayList<DataManager>();

        ObjectsCount = 100000;
        ServerMain app = new ServerMain();
        app.setShowSettings(false);
        app.setPauseOnLostFocus(false);
        app.start();
    }

    @Override
    @SuppressWarnings("empty-statement")
    public void simpleInitApp() {
        getFlyByCamera().setEnabled(false);

        while (takeNewClients() != true);

        seekerRadius = 10;
        wandererRadius = 10;
        blackHoleRadius = 10;
        bulletRadius = 10;

        playerNode = new Node("players");
        bulletNode = new Node("bullets");
        enemyNode = new Node("enemies");
        blackHoleNode = new Node("black holes");

        guiNode.attachChild(playerNode);
        guiNode.attachChild(bulletNode);
        guiNode.attachChild(enemyNode);
        guiNode.attachChild(blackHoleNode);
    }

    private void connect_player(DataManager dm) {
        String msg = String.valueOf(width) + " " + String.valueOf(height);
        dm.SendMessage(
                DataManager.MessageCode.SimpleInitApp.value(),
                DataManager.MessageCode.WidthAndHeight.value(),
                msg);

        Spatial player = new Node("player");

        Boolean notfind = true;
        while (notfind) {
            msg = dm.FindRecord(
                    DataManager.MessageCode.SimpleInitApp.value(),
                    DataManager.MessageCode.PlayerRadius.value());
            if (!msg.equals(""))
                notfind = false;
        }
        player.setUserData("objid", ++ObjectsCount);
        player.setUserData("radius", Float.parseFloat(msg) );
        player.addControl(new PlayerControl(dm, width, height));
        dm.SendMessage(
                DataManager.MessageCode.SimpleInitApp.value(),
                DataManager.MessageCode.PlayerID.value(),
                String.valueOf(player.getUserData("objid")));

        player.setUserData("alive", true);
        player.move(width/2, height/2, 0f);

        player.setUserData("dm", dm);
        playerNode.attachChild(player);
    }

    private void connect_observer(DataManager dm) {
        String msg = "";
        msg = String.valueOf(width) +
              " " +
              String.valueOf(height);

        dm.SendMessage(
                DataManager.MessageCode.Observer.value(),
                DataManager.MessageCode.WidthAndHeight.value(),
                msg);
    }

    private Boolean takeNewClients() {
        int delta = (observersCount + playersCount) - clistener.getNumClients();

        for (int i = 0; i < delta; i++) {
            DataManager dm = clistener.dms.get((observersCount + playersCount) + i);

            String msg = "";
            while (msg.equals("")) {
                msg = dm.FindRecord(
                        DataManager.MessageCode.SimpleInitApp.value(),
                    DataManager.MessageCode.WhoAreYou.value());
            }

            if (msg.equals("observer")) {
                connect_observer(dm);
                odms.add(dm);
            } else {
                connect_player(dm);
                pdms.add(dm);
            }
        }
        return false;
    }

    private Boolean gameIsOver() {
        gameOver = false;
        for (int i = 0; i < playerNode.getQuantity(); i++) {
            gameOver = gameOver || (Boolean)playerNode.getChild(i).getUserData("alive");
        }
        return gameOver;
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (!gameIsOver()) {
            spawnEnemies();
            spawnBlackHoles();
            for (int i = 0; i < playerNode.getQuantity(); i++) {
                Spatial player = playerNode.getChild(i);
                spawnBullets(player);
            }
            handleCollisions();
            handleGravity(tpf);
        }
        takeNewClients();

        /*
        if ((Boolean) player.getUserData("alive")) {
            spawnEnemies();
            spawnBlackHoles();
            spawnBullets();
            handleCollisions();
            handleGravity(tpf);
        }
        *
        *
        *
         * Возможно респаунем потом
         *
         *
         *
         *
         * else if ((System.currentTimeMillis() -
                (Long)player.getUserData("dieTime")) > 4000f
                && !gameOver) {
            // spawn player
            player.setLocalTranslation(
                    width / 2f,
                    height / 2f,
                    0f);
            guiNode.attachChild(player);
            player.setUserData("alive", true);
        }*/
        // send info to client
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    public void Log(String s) {
        System.out.println(s);
    }

    public static float getAngleFromVector(Vector3f vec) {
        Vector2f vec2 = new Vector2f(vec.x, vec.y);
        return vec2.getAngle();
    }

    public static Vector3f getVectorFromAngle(float angle) {
        return new Vector3f(FastMath.cos(angle), FastMath.sin(angle), 0f);
    }

    private void spawnBullets(Spatial player)
    {
        DataManager dm = (DataManager)player.getUserData("dm");
        List<String> msgs = dm.FindAllRecords(
                            DataManager.MessageCode.OnAnalog.value(),
                            DataManager.MessageCode.CreateBullet.value());

        for (int i = 0; i < msgs.size(); i++)
        {
            String[] split = msgs.get(i).split(" ");
            Vector3f aim = new Vector3f(Float.valueOf(split[0]),
                    Float.valueOf(split[1]),
                    Float.valueOf(split[2]));
            Vector3f offset = new Vector3f(aim.y/3, -aim.x/3, 0f);

            Spatial bullet = new Node("Bullet");
            bullet.setUserData("objid", ++ObjectsCount);
            bullet.setUserData("radius", bulletRadius);
            Vector3f finalOffset = aim.add(offset).mult(30);
            Vector3f trans =
                    player.getLocalTranslation().add(finalOffset);
            bullet.setLocalTranslation(trans);
            bullet.addControl(
                    new BulletControl(dm, aim,
                                      settings.getWidth(),
                                      settings.getHeight()));
            bulletNode.attachChild(bullet);

            String msg = String.valueOf(bullet.getUserData("objid") + " " +
                         String.valueOf(trans.x) + " " +
                         String.valueOf(trans.y) + " " +
                         String.valueOf(trans.z));

            dm.SendMessage(
                            DataManager.MessageCode.OnAnalog.value(),
                            DataManager.MessageCode.CreateBullet.value(),
                            msg);

            Spatial bullet2 = new Node("Bullet");
            finalOffset = aim.add(offset.negate()).mult(30);
            trans = player.getLocalTranslation().add(finalOffset);
            bullet2.setLocalTranslation(trans);
            bullet2.setUserData("objid", ++ObjectsCount);
            bullet2.setUserData("radius", bulletRadius);
            bullet2.addControl(
                    new BulletControl(dm,
                    aim, settings.getWidth(), settings.getHeight()));
            bulletNode.attachChild(bullet2);

            msg = String.valueOf(bullet2.getUserData("objid") + " " +
                         String.valueOf(trans.x) + " " +
                         String.valueOf(trans.y) + " " +
                         String.valueOf(trans.z));

            dm.SendMessage(
                            DataManager.MessageCode.OnAnalog.value(),
                            DataManager.MessageCode.CreateBullet.value(),
                            msg);

        }
    }
    private void spawnEnemies() {
        if (System.currentTimeMillis() - enemySpawnCooldown >= 17) {
            enemySpawnCooldown = System.currentTimeMillis();

            if (enemyNode.getQuantity() < 50) {
                if (new Random().nextInt((int)enemySpawnChance) == 0) {
                    createSeeker();
                }
                if (new Random().nextInt((int)enemySpawnChance) == 0) {
                    createWanderer();
                }
            }
            // increase Spawn Time
            if (enemySpawnChance >= 1.1f) {
                enemySpawnChance -= 0.005f;
            }
        }
    }

    private void createSeeker() {
        Spatial seeker = new Node("Seeker");
        seeker.setLocalTranslation(getSpawnPosition());
        int player = new Random().nextInt(playerNode.getQuantity());
        seeker.addControl(new SeekerControl(playerNode.getChild(player),
                                            pdms, odms));
        seeker.setUserData("active", false);
        seeker.setUserData("radius", seekerRadius);
        seeker.setUserData("objid", ++ObjectsCount);

        // Send spawnPosition and Objects count
        long id = seeker.getUserData("objid");
        Vector3f tr = seeker.getLocalTranslation();
        String msg = String.valueOf(id)
                + " " + String.valueOf(tr.x)
                + " " + String.valueOf(tr.y)
                + " " + String.valueOf(tr.z);

        for (int i = 0; i < playersCount; i++) {
            DataManager dm = pdms.get(i);
            dm.SendMessage(
                DataManager.MessageCode.SpawnFunction.value(),
                DataManager.MessageCode.CreateSeeker.value(),
                msg);
        }

        for (int i = 0; i < observersCount; i++) {
            DataManager dm = odms.get(i);
            dm.SendMessage(
                DataManager.MessageCode.SpawnFunction.value(),
                DataManager.MessageCode.CreateSeeker.value(),
                msg);
        }

        enemyNode.attachChild(seeker);
    }

    private void createWanderer() {
        Spatial wanderer = new Node("Wanderer");
        wanderer.setLocalTranslation(getSpawnPosition());
        wanderer.addControl(new WandererControl(pdms, odms, width, height));
        wanderer.setUserData("active", false);
        wanderer.setUserData("radius", wandererRadius);
        wanderer.setUserData("objid", ++ObjectsCount);

        // Send spawnPosition and Objects count
        long id = wanderer.getUserData("objid");
        Vector3f tr = wanderer.getLocalTranslation();
        String msg = String.valueOf(id)
                + " " + String.valueOf(tr.x)
                + " " + String.valueOf(tr.y)
                + " " + String.valueOf(tr.z);

        for (int i = 0; i < observersCount; i++) {
            DataManager dm = odms.get(i);
            dm.SendMessage(
                DataManager.MessageCode.SpawnFunction.value(),
                DataManager.MessageCode.CreateWanderer.value(),
                msg);
        }

        for (int i = 0; i < playersCount; i++) {
            DataManager dm = pdms.get(i);
            dm.SendMessage(
                DataManager.MessageCode.SpawnFunction.value(),
                DataManager.MessageCode.CreateWanderer.value(),
                msg);
        }

        enemyNode.attachChild(wanderer);
    }

    private Vector3f getSpawnPosition() {
        Vector3f pos;
        /*
         * Не знаю как просто обобщить...
         *
         * do {
            pos = new Vector3f(new Random().nextInt(settings.getWidth()),
                    new Random().nextInt(settings.getHeight()), 0);
        } while (pos.distanceSquared(player.getLocalTranslation()) < 8000);*/
        pos = new Vector3f(new Random().nextInt(settings.getWidth()),
                    new Random().nextInt(settings.getHeight()), 0);
        return pos;
    }

    private void handleCollisions() {
        // should the player die?
        for (int i = 0; i < enemyNode.getQuantity(); i++) {
            if ((Boolean) enemyNode.getChild(i).getUserData("active")) {
                for (int j = 0; j < playerNode.getQuantity(); j++) {
                    Spatial player = playerNode.getChild(j);
                    if (checkCollision(player,
                                        enemyNode.getChild(i))) {
                        DataManager dm = player.getUserData("dm");
                        dm.SendMessage(
                                DataManager.MessageCode.HandleCollision.value(),
                                DataManager.MessageCode.PlayerDie.value(),
                                "playerDie");
                        killPlayer(player);
                        return;
                    }
                }
            }

            // should an enemy die?
            for (int j = 0; j < bulletNode.getQuantity(); j++) {
                if (checkCollision(enemyNode.getChild(i),
                        bulletNode.getChild(j))) {
                    // add points depending on the type of enemy
                    if (enemyNode.getChild(i).getName().equals("Seeker")) {

                    } else if (enemyNode.getChild(i).getName().equals(
                            "Wanderer")) {

                    }

                    for (int k = 0; k < playersCount; k++) {
                        DataManager dm = pdms.get(k);
                        dm.SendMessage(
                            DataManager.MessageCode.HandleCollision.value(),
                            DataManager.MessageCode.EnemyDie.value(),
                            String.valueOf(
                                enemyNode.getChild(i).getUserData("objid")));
                        dm.SendMessage(
                            DataManager.MessageCode.HandleCollision.value(),
                            DataManager.MessageCode.BulletDie.value(),
                            String.valueOf(
                                bulletNode.getChild(j).getUserData("objid")));
                    }

                    for (int k = 0; k < observersCount; k++) {
                        DataManager dm = odms.get(k);
                        dm.SendMessage(
                            DataManager.MessageCode.HandleCollision.value(),
                            DataManager.MessageCode.EnemyDie.value(),
                            String.valueOf(
                                enemyNode.getChild(i).getUserData("objid")));
                        dm.SendMessage(
                            DataManager.MessageCode.HandleCollision.value(),
                            DataManager.MessageCode.BulletDie.value(),
                            String.valueOf(
                                bulletNode.getChild(j).getUserData("objid")));
                    }

                    enemyNode.detachChildAt(i);
                    bulletNode.detachChildAt(j);
                    break;
                }
            }
        }

        // is something colliding with a black hole?
        for (int i = 0; i < blackHoleNode.getQuantity(); i++) {
            Spatial blackHole = blackHoleNode.getChild(i);
            if ((Boolean) blackHole.getUserData("active")) {
                // player
                for (int j = 0; j < playerNode.getQuantity(); j++) {
                    Spatial player = playerNode.getChild(j);
                    if (checkCollision(player, blackHole)) {
                        DataManager dm = player.getUserData("dm");
                        dm.SendMessage(
                                DataManager.MessageCode.HandleCollision.value(),
                                DataManager.MessageCode.PlayerDie.value(),
                                "playerDie");
                        killPlayer(player);
                        return;
                    }
                }

                // enemies
                for (int j = 0; j < enemyNode.getQuantity(); j++) {
                    if (checkCollision(enemyNode.getChild(j), blackHole)) {
                        for (int k = 0; k < playersCount; k++) {
                            DataManager dm = pdms.get(k);
                            dm.SendMessage(
                                DataManager.MessageCode.HandleCollision.value(),
                                DataManager.MessageCode.EnemyDie.value(),
                                String.valueOf(
                                   enemyNode.getChild(j).getUserData("objid")));
                        }
                        for (int k = 0; k < observersCount; k++) {
                            DataManager dm = odms.get(k);
                            dm.SendMessage(
                                DataManager.MessageCode.HandleCollision.value(),
                                DataManager.MessageCode.EnemyDie.value(),
                                String.valueOf(
                                   enemyNode.getChild(j).getUserData("objid")));
                        }
                        enemyNode.detachChildAt(j);
                    }
                }

                // bullets
                for (int j = 0; j < bulletNode.getQuantity(); j++) {
                    if (checkCollision(bulletNode.getChild(j), blackHole)) {
                        for (int k = 0; k < playersCount; k++) {
                            DataManager dm = pdms.get(k);
                            dm.SendMessage(
                                DataManager.MessageCode.HandleCollision.value(),
                                DataManager.MessageCode.BulletDie.value(),
                                String.valueOf(
                                  bulletNode.getChild(j).getUserData("objid")));
                        }
                        for (int k = 0; k < observersCount; k++) {
                            DataManager dm = odms.get(k);
                            dm.SendMessage(
                                DataManager.MessageCode.HandleCollision.value(),
                                DataManager.MessageCode.BulletDie.value(),
                                String.valueOf(
                                  bulletNode.getChild(j).getUserData("objid")));
                        }

                        bulletNode.detachChildAt(j);
                        blackHole.getControl(BlackHoleControl.class).wasShot();
                        if (blackHole.getControl(
                                BlackHoleControl.class).isDead()) {
                            for (int k = 0; k < playersCount; k++) {
                                DataManager dm = pdms.get(k);
                                dm.SendMessage(
                                    DataManager.MessageCode.HandleCollision.value(),
                                    DataManager.MessageCode.BlackHoleDie.value(),
                                    String.valueOf(blackHole.getUserData("objid")));
                            }
                            for (int k = 0; k < observersCount; k++) {
                                DataManager dm = odms.get(k);
                                dm.SendMessage(
                                    DataManager.MessageCode.HandleCollision.value(),
                                    DataManager.MessageCode.BlackHoleDie.value(),
                                    String.valueOf(blackHole.getUserData("objid")));
                            }
                            blackHoleNode.detachChild(blackHole);
                            break;
                        }
                    }
                }
            }
        }
    }

    private boolean checkCollision(Spatial a, Spatial b) {
        float distance = a.getLocalTranslation().distance(
                b.getLocalTranslation());
        float maxDistance = (Float)a.getUserData("radius") +
                (Float)b.getUserData("radius");
        return distance <= maxDistance;
    }

    private void killPlayer(Spatial player) {
        player.removeFromParent();
        player.getControl(PlayerControl.class).reset();
        player.setUserData("alive", false);
        player.setUserData("dieTime", System.currentTimeMillis());
        enemyNode.detachAllChildren();
        blackHoleNode.detachAllChildren();
    }

    private void spawnBlackHoles() {
        if (blackHoleNode.getQuantity() < 2) {
            if (System.currentTimeMillis() - spawnCooldownBlackHole > 10f) {
                spawnCooldownBlackHole = System.currentTimeMillis();
                if (new Random().nextInt(1000) == 0) {
                    createBlackHole();
                }
            }
        }
    }

    private void createBlackHole() {
        Spatial blackHole = new Node("Black Hole");
        blackHole.setLocalTranslation(getSpawnPosition());
        blackHole.addControl(new BlackHoleControl(pdms, odms));
        blackHole.setUserData("active", false);
        blackHole.setUserData("radius", blackHoleRadius);
        blackHole.setUserData("objid", ++ObjectsCount);

        // Send spawnPosition and Objects count
        long id = blackHole.getUserData("objid");
        Vector3f tr = blackHole.getLocalTranslation();
        String msg = String.valueOf(id)
                + " " + String.valueOf(tr.x)
                + " " + String.valueOf(tr.y)
                + " " + String.valueOf(tr.z);

        for (int i = 0; i < playersCount; i++) {
            DataManager dm = pdms.get(i);
            dm.SendMessage(
                DataManager.MessageCode.SpawnFunction.value(),
                DataManager.MessageCode.CreateBlackHole.value(),
                msg);
        }

        blackHoleNode.attachChild(blackHole);
    }

    private void handleGravity(float tpf) {
        for (int i = 0; i < blackHoleNode.getQuantity(); i++) {
            Spatial blackHole = blackHoleNode.getChild(i);
            if (!(Boolean)blackHole.getUserData("active")) {
                continue;
            }
            int radius = 250;

            // check player
            for (int j = 0; j < playerNode.getQuantity(); i++) {
                Spatial player = playerNode.getChild(i);
                if (isNearby(player, blackHole, radius)) {
                    applyGravity(blackHole, player, tpf);
                }
            }

            // check bullets
            for (int j = 0; j < bulletNode.getQuantity(); j++) {
                Spatial bullet = bulletNode.getChild(j);
                if (isNearby(bullet, blackHole, radius)) {
                    applyGravity(blackHole, bullet, tpf);
                }
            }
        }
    }

    private boolean isNearby(Spatial a, Spatial b, float distance) {
        Vector3f pos_a = a.getLocalTranslation();
        Vector3f pos_b = b.getLocalTranslation();
        return (pos_a.distanceSquared(pos_b) <= distance * distance);
    }

    private void applyGravity(Spatial blackHole, Spatial target, float tpf) {
        Vector3f diff = blackHole.getLocalTranslation().subtract(
                target.getLocalTranslation());
        Vector3f gravity = diff.normalize().multLocal(tpf);
        float distance = diff.length();


        if (target.getName().equals("Player")) {
            gravity.multLocal(250f / distance);
            target.getControl(PlayerControl.class).applyGravity(
                    gravity.mult(80f));
        } else if (target.getName().equals("Bullet")) {
            gravity.multLocal(250f / distance);
            target.getControl(BulletControl.class).applyGravity(
                    gravity.mult(-0.8f));
        } else if (target.getName().equals("Seeker")) {
            target.getControl(SeekerControl.class).applyGravity(
                    gravity.mult(150000f));
        } else if (target.getName().equals("Wanderer")) {
            target.getControl(WandererControl.class).applyGravity(
                    gravity.mult(150000f));
        }
    }
}
