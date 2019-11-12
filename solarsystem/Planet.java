package solarsystem;

import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;

import java.io.File;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import graphicslib3D.GLSLUtils;
import graphicslib3D.Vector3D;

public class Planet{
	protected GL4 gl = (GL4) GLContext.getCurrentGL();
	protected GerstnerSystem sys;
	protected GLSLUtils util = new GLSLUtils();
	
	protected double orbitSpeed;
	protected double rotateSpeed;
	private Vector3D startLocation;
	
	private Texture planetTex;
	protected int tex;
	private Texture altTex;
	protected int alttex;
	private boolean hasAltTexture = false;
	private boolean hasSwappedThisFrame = false;

	public Planet(GerstnerSystem sys, Vector3D pos, String textureLoc, double orbitSpeed, double rotateSpeed) {
		this.sys = sys;
		startLocation = pos;
		planetTex = loadTexture(textureLoc);
		tex = planetTex.getTextureObject();
		this.orbitSpeed = orbitSpeed;
		this.rotateSpeed = rotateSpeed;
	}
	
	//+++++++++++++++++++++++
	// TRANSFORMATIONS
	//+++++++++++++++++++++++
	
	public void Translate(double x, double y, double z, double amt) {
		sys.mvStack.translate(Math.sin(amt*orbitSpeed)*x, y, Math.cos(amt*orbitSpeed)*z);
	}
	
	public void Rotate(double x, double y, double z, double w) {
		sys.mvStack.rotate(x*rotateSpeed,y*rotateSpeed,z*rotateSpeed,w*rotateSpeed); //Rotate sun
	}

	public void ScaleUniform(double amt) {
		sys.mvStack.scale(amt, amt, amt);
	}
	
	//+++++++++++++++++++++++
	// TEXTURE CODE
	//+++++++++++++++++++++++
	
	public void SwapTexture() {
		if(hasAltTexture && !hasSwappedThisFrame) {
		Texture tempTex = planetTex;
		int temptex = tex;
			
		planetTex = altTex;
		tex = alttex;
		
		altTex = tempTex;
		alttex = temptex;
		
		hasSwappedThisFrame = true;
		}
	}
	
	public void ResetSwapStatus() {
		hasSwappedThisFrame = false;
	}
	
	public Texture loadTexture(String textureFileName)
	{	Texture tex = null;
		try { 
			tex = TextureIO.newTexture(new File(textureFileName), false);
		}
		catch (Exception e) { e.printStackTrace(); }
		return tex;
	}
	
	public void enableTextureSwap(String altLoc) {
		altTex = loadTexture(altLoc);
		alttex = altTex.getTextureObject();
		hasAltTexture = true;
	}

}
