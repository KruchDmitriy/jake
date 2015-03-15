package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector2f;
import com.jme3.scene.Mesh;
import com.jme3.util.BufferUtils;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.light.DirectionalLight;

/**
 * test
 * @author normenhansen
 */
public class Main extends SimpleApplication {

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        viewPort.setBackgroundColor(ColorRGBA.LightGray);
        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.1f, -1f, -1).normalizeLocal());
        rootNode.addLight(dl);
        
        Mesh mesh = new Mesh();
        Vector3f [] vertices = new Vector3f[4];
        vertices[0] = new Vector3f(-5,-5,-5);
        vertices[1] = new Vector3f(5,-5,-5);
        vertices[2] = new Vector3f(-5,-5,5);
        vertices[3] = new Vector3f(5,-5,5);
        
        Vector2f[] texCoord = new Vector2f[4];
        texCoord[0] = new Vector2f(0,0);
        texCoord[1] = new Vector2f(1,0);
        texCoord[2] = new Vector2f(0,1);
        texCoord[3] = new Vector2f(1,1);
        
        int [] indexes = { 1,0,2, 2,3,1 };
        
        mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
        mesh.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoord));
        mesh.setBuffer(Type.Index,    3, BufferUtils.createIntBuffer(indexes));
        mesh.updateBound();
        
        Geometry geo = new Geometry("OurMesh", mesh); // using our custom mesh object
        Material mat = new Material(assetManager, 
            "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geo.setMaterial(mat);
        rootNode.attachChild(geo);
        
        /*Box b = new Box(1, 1, 1);
        Geometry geom = new Geometry("Box", b);

        Material mater = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mater.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mater);

        rootNode.attachChild(geom);*/
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
