package solarsystem;

import graphicslib3D.*;
import graphicslib3D.GLSLUtils.*;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.nio.*;
import javax.swing.*;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.common.nio.Buffers;

public class GerstnerSystem extends JFrame implements GLEventListener, KeyListener
{	private GLCanvas myCanvas;
	public int rendering_program;
	public int vao[] = new int[1];
	public int vbo[] = new int[21];
	public int nextvbo = 0;
	private GLSLUtils util = new GLSLUtils();
	
	//CAMERA
	private Vector3D camStartPos;
	private Camera myCam;
	
	//TIME
	private long startTime;
	
	//AXIS
	private boolean axisEnabled;
	
	private Texture redTex;
    private int redTexture;
    private Texture greenTex;
    private int greenTexture;
    private Texture blueTex;
    private int blueTexture;
	
	//AZALEA (SUN)
	private SpherePlanet azalea;
	private Vector3D azalea_pos = new Vector3D(0.0,0.0,0.0);
	private String azalea_tex_loc = "src/solarsystem/azalea.jpg";
	private double az_orb_sp = 0.0;
	private double az_rot_sp = 0.2;
	private float azalea_size = 4.5f;
	
	//DAHLIA (PLANET 1)
	private SpherePlanet dahlia;
	private Vector3D dahlia_pos = new Vector3D(0.0,0.0,0.0);
	private String dahlia_tex_loc = "src/solarsystem/dahlia.jpg";
	private double da_orb_sp = 0.4;
	private double da_rot_sp = 0.5;
	private float dahlia_size = 2.0f;
	
	//MYRTLE (DAHLIA'S MOON)
	private SpherePlanet myrtle;
	private Vector3D myrtle_pos = new Vector3D(0.0,0.0,0.0);
	private String myrtle_tex_loc = "src/solarsystem/myrtle.jpg";
	private double my_orb_sp = 0.4;
	private double my_rot_sp = 0.5;
	private float myrtle_size = 0.6f;
	
	//ZINNIA (PENTAGON PLANET)
	private PentagonalPlanet zinnia;
	private Vector3D zinnia_pos = new Vector3D(0.0,0.0,0.0);
	private String zinnia_tex_loc = "src/solarsystem/zinnia.jpg";
	private String zinnia_alt_tex_loc = "src/solarsystem/kevin.png";
	private double zi_orb_sp = 0.1;
	private double zi_rot_sp = 0.3;
	private float zinnia_size = 1.5f;
	
	//ASTER (TILTED PLANET)
	private SpherePlanet aster;
	private Vector3D aster_pos = new Vector3D(0.0,0.0,0.0);
	private String aster_tex_loc = "src/solarsystem/aster.jpg";
	private double as_orb_sp = 0.5;
	private double as_rot_sp = 0.5;
	private float aster_size = 1.5f;
	
	//CLOVER (TILTED PLANET)
	private PentagonalPlanet clover;
	private Vector3D clover_pos = new Vector3D(0.0,0.0,0.0);
	private String clover_tex_loc = "src/solarsystem/clover.jpg";
	private double cl_orb_sp = 0.5;
	private double cl_rot_sp = 1.0;
	private float clover_size = 0.3f;
	
	public MatrixStack mvStack = new MatrixStack(20);

	public GerstnerSystem()
	{	
		//Window Settings
		setTitle("Gerstner System");
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(screenSize.width, screenSize.height);
		
		//Making sure we get a GL4 context for the canvas
        GLProfile profile = GLProfile.get(GLProfile.GL4);
        GLCapabilities capabilities = new GLCapabilities(profile);
		myCanvas = new GLCanvas(capabilities);
		
		myCanvas.addGLEventListener(this);
		getContentPane().add(myCanvas);
		this.setVisible(true);
		
		//Keyboard Presses
		myCanvas.setFocusable(true);
		myCanvas.requestFocusInWindow();
		myCanvas.addKeyListener(this);
		
		//Set up Animation
		FPSAnimator animator = new FPSAnimator(myCanvas, 50);
		animator.start();
	}
	
	public void init(GLAutoDrawable drawable)
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();
		rendering_program = createShaderProgram();
		
		camStartPos = new Vector3D(1.0,2.0,20.0,0.0);
		myCam = new Camera(this, camStartPos);
		
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(vbo.length, vbo, 0);
		
		axisEnabled = true;
		setupAxis();
		blueTex = loadTexture("src/solarsystem/blue.jpg");
		blueTexture = blueTex.getTextureObject();
		greenTex = loadTexture("src/solarsystem/green.jpg");
		greenTexture = greenTex.getTextureObject();
		redTex = loadTexture("src/solarsystem/red.jpg");
		redTexture = redTex.getTextureObject();
		
		azalea = new SpherePlanet(this,azalea_pos,azalea_tex_loc,az_orb_sp, az_rot_sp);
		dahlia = new SpherePlanet(this,dahlia_pos,dahlia_tex_loc,da_orb_sp, da_rot_sp);
		zinnia = new PentagonalPlanet(this,zinnia_pos,zinnia_tex_loc,zi_orb_sp,zi_rot_sp);
		zinnia.enableTextureSwap(zinnia_alt_tex_loc);
		aster = new SpherePlanet(this,aster_pos,aster_tex_loc,as_orb_sp, as_rot_sp);
		myrtle = new SpherePlanet(this,myrtle_pos,myrtle_tex_loc,my_orb_sp, my_rot_sp);
		clover = new PentagonalPlanet(this,clover_pos,clover_tex_loc,cl_orb_sp,cl_rot_sp);
		
		startTime = System.currentTimeMillis();
	}
	
	public void setupAxis() {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		float[] x_axis = {
                -200.0f, 0.0f, 0.0f, 200.0f, 0.0f, 0.0f,
        };
        float[] y_axis = {
                0.0f, -200.0f, 0.0f, 0.0f, 200.0f, 0.0f,
        };
        float[] z_axis = {
                0.0f, 0.0f, -200.0f, 0.0f, 0.0f, 200.0f
        };
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[nextvbo++]);
        FloatBuffer xAxisBuf = Buffers.newDirectFloatBuffer(x_axis);
        gl.glBufferData(GL_ARRAY_BUFFER, xAxisBuf.limit()*4, xAxisBuf, GL_STATIC_DRAW);
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[nextvbo++]);
        FloatBuffer yAxisBuf = Buffers.newDirectFloatBuffer(y_axis);
        gl.glBufferData(GL_ARRAY_BUFFER, xAxisBuf.limit()*4, yAxisBuf, GL_STATIC_DRAW);
        
        gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[nextvbo++]);
        FloatBuffer zAxisBuf = Buffers.newDirectFloatBuffer(z_axis);
        gl.glBufferData(GL_ARRAY_BUFFER, xAxisBuf.limit()*4, zAxisBuf, GL_STATIC_DRAW);
        
	}

	public void display(GLAutoDrawable drawable)
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();

		//Write Background to screen
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		float bkg[] = { 0.0f, 0.0f, 0.0f, 1.0f };
		FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
		gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);
		gl.glClear(GL_DEPTH_BUFFER_BIT);

		gl.glUseProgram(rendering_program);

		int mv_loc = gl.glGetUniformLocation(rendering_program, "mv_matrix");
		int proj_loc = gl.glGetUniformLocation(rendering_program, "proj_matrix");

		float aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		Matrix3D pMat = myCam.perspective(60.0f, aspect, 0.1f, 1000.0f);
		
		//Update the camera position
		myCam.updateCamera();
		
		// push GLOBAL MATRIX onto the stack
		mvStack.pushMatrix(); //Save global reference system
		
		//Get the look-at matrix and multiply
		Matrix3D mvMat = myCam.lookOverHere();
		mvStack.multMatrix(mvMat);
	
		//Move camera and push VIEW MATRIX onto the stack
		myCam.moveCam();
		
		//Push projection matrix to the vertex shader
		gl.glUniformMatrix4fv(proj_loc, 1, false, pMat.getFloatValues(), 0);
		gl.glUniformMatrix4fv(mv_loc, 1, false, mvMat.getFloatValues(), 0);
		
		//Time elapsed figure
		double amt = (double)(System.currentTimeMillis())/1000.0;
		
		// ---------------------- AZALEA (SUN) ----------------------------
		mvStack.pushMatrix(); //Save camera's reference system
		azalea.Translate(azalea_pos.getX(),azalea_pos.getY(),azalea_pos.getZ(),amt); //Translate Azalea to Start Position
		mvStack.pushMatrix(); //Save Azalea's Position
		//azalea.Rotate((System.currentTimeMillis())/10.0, 1.0, 0.0, 0.0); //Rotate Azalea
		azalea.ScaleUniform(azalea_size);
		azalea.DisplayPlanet();
		mvStack.popMatrix(); //Remove Scale and Rotate -> back to Azalea position
		mvStack.popMatrix(); //Back to Camera
		
		// ----------------------- AXIS -----------------------------------
		if (axisEnabled) {
            //x-axis
			mvStack.pushMatrix();
            //gl.glUniformMatrix4fv(mv_loc, 1, false, mvStack.peek().getFloatValues(), 0);
            gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
            gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            gl.glEnableVertexAttribArray(0);
            gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
            gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
            gl.glEnableVertexAttribArray(1);
            gl.glActiveTexture(GL_TEXTURE0);
            gl.glBindTexture(GL_TEXTURE_2D, redTexture);
            gl.glDrawArrays(GL_LINES, 0, 2);
            gl.glDrawArrays(GL_LINES, 0, 2);

            //y-axis
            gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
            gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            gl.glEnableVertexAttribArray(0);
            gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
            gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
            gl.glEnableVertexAttribArray(1);
            gl.glActiveTexture(GL_TEXTURE0);
            gl.glBindTexture(GL_TEXTURE_2D, greenTexture);
            gl.glDrawArrays(GL_LINES, 0, 2);
            gl.glDrawArrays(GL_LINES, 0, 2);

            //z-axis
            gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
            gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            gl.glEnableVertexAttribArray(0);
            gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
            gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
            gl.glEnableVertexAttribArray(1);
            gl.glActiveTexture(GL_TEXTURE0);
            gl.glBindTexture(GL_TEXTURE_2D, blueTexture);
            gl.glDrawArrays(GL_LINES, 0, 2);
            gl.glDrawArrays(GL_LINES, 0, 2);
            
            mvStack.popMatrix();
        }
		
		// ----------------------  DAHLIA (PLANET 1) -----------------------------------
		mvStack.pushMatrix(); //Save Azalea Reference
		dahlia.Rotate(25.0, 0.0, 0.0, 1.0);
		dahlia.Translate(16.0,0.0,14.0,amt); //Travels in an orbit, determined by sins and cosines (found in Planet class)
		mvStack.pushMatrix(); //Save Dahlia's position
		dahlia.Rotate((System.currentTimeMillis())/10.0, 1.0, 0.0, 0.0);
		dahlia.ScaleUniform(dahlia_size);
		dahlia.DisplayPlanet();
		mvStack.popMatrix(); //Go back to Dahlia's position (no rotation)
		
		// ----------------------  MYRTLE (DAHLIA MOON) -----------------------------------
		mvStack.pushMatrix(); //Push Dahlia
		myrtle.Rotate(75.0, 0.0, 0.0, 1.0); //Rotate 75 deg from Dahlia's axis of rotation
		myrtle.Translate(5.0,0.0,4.0,amt);
		myrtle.Rotate(-(System.currentTimeMillis())/10.0, 1.0, 0.0, 0.0);
		myrtle.ScaleUniform(myrtle_size);
		myrtle.DisplayPlanet();
		mvStack.popMatrix(); //Go back to Dahlia
		mvStack.popMatrix(); //Go back to Azalea
		
		// ---------------------- ZINNIA (PENTAGON PLANET) ------------------------------------
		mvStack.pushMatrix(); //Save Azalea Ref
		zinnia.Translate(20.0, 0.0, 10.0, amt);
		mvStack.pushMatrix(); //Save Zinnia's position
		zinnia.Rotate((System.currentTimeMillis())/10.0, 1.0, 0.0, 0.0);
		zinnia.ScaleUniform(zinnia_size);
		//This block swaps the texture every 2 seconds
		if(((System.currentTimeMillis()-startTime) / 1000) % 2 == 0) {
			zinnia.SwapTexture();
		} else {zinnia.ResetSwapStatus();}
		//
		zinnia.DisplayPlanet();
		mvStack.popMatrix(); //Go back to Zinnia's position (no rotation)
		mvStack.popMatrix(); //Go back to Azalea
		
		// ---------------------- ASTER (TILTED PLANET) --------------------------------
		mvStack.pushMatrix(); //Save Azalea Ref
		aster.Rotate(-45.0,0.0,0.0,1.0); //Create orbit plane 25 deg around Z axis
		aster.Translate(15.0, 0.0, 8.0, amt);
		mvStack.pushMatrix();
		aster.Rotate((2*System.currentTimeMillis())/10.0, 0.0, 1.0, 0.0);
		aster.ScaleUniform(aster_size);
		aster.DisplayPlanet();
		mvStack.popMatrix();
		
		// ----------------------  CLOVER (ASTER MOON) -----------------------------------
		mvStack.pushMatrix(); //Push Aster
		clover.Translate(3.0,0.0,3.0,amt);
		clover.Rotate((System.currentTimeMillis())/10.0, 1.0, 0.0, 0.0);
		clover.ScaleUniform(clover_size);
		clover.DisplayPlanet();
		mvStack.popMatrix(); //Go back to Aster
		
		// ----------------- CLEAN UP ------------------------------------
		mvStack.popMatrix();//Go backto camera's reference
		mvStack.popMatrix();//Go backto global reference
	}
	
	public static void main(String[] args) { new GerstnerSystem(); }
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
	public void dispose(GLAutoDrawable drawable) {}

	private int createShaderProgram()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();

		String vshaderSource[] = util.readShaderSource("src/solarsystem/vert.shader");
		String fshaderSource[] = util.readShaderSource("src/solarsystem/frag.shader");

		int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
		int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);

		gl.glShaderSource(vShader, vshaderSource.length, vshaderSource, null, 0);
		gl.glShaderSource(fShader, fshaderSource.length, fshaderSource, null, 0);

		gl.glCompileShader(vShader);
		gl.glCompileShader(fShader);

		int vfprogram = gl.glCreateProgram();
		gl.glAttachShader(vfprogram, vShader);
		gl.glAttachShader(vfprogram, fShader);
		gl.glLinkProgram(vfprogram);
		return vfprogram;
	}
	
	public Texture loadTexture(String textureFileName)
	{	Texture tex = null;
		try { 
			tex = TextureIO.newTexture(new File(textureFileName), false);
		}
		catch (Exception e) { e.printStackTrace(); }
		return tex;
	}
	
	public void toggleWorldAxis() {
		if(axisEnabled) {axisEnabled = false;}
		else {axisEnabled = true;}
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {
		System.out.println("PRESSED");
		int code = e.getKeyCode();
		
		switch(code) {
		case KeyEvent.VK_W:
			myCam.moveCamForward();
			break;
		case KeyEvent.VK_S:
			myCam.moveCamBackwards();
			break;
		case KeyEvent.VK_A:
			myCam.moveCamLeft();
			break;
		case KeyEvent.VK_D:
			myCam.moveCamRight();
			break;
		case KeyEvent.VK_E:
			myCam.moveCamDown();
			break;
		case KeyEvent.VK_Q:
			myCam.moveCamUp();
			break;
		case KeyEvent.VK_UP:
			myCam.pitchCamUp();
			break;
		case KeyEvent.VK_DOWN:
			myCam.pitchCamDown();
			break;
		case KeyEvent.VK_LEFT:
			myCam.panCamLeft();
			break;
		case KeyEvent.VK_RIGHT:
			myCam.panCamRight();
			break;
		case KeyEvent.VK_SPACE:
			toggleWorldAxis();
			break;
		default:
		}
        
	}

	@Override
	public void keyReleased(KeyEvent e) {
		System.out.println("Released");
		int code = e.getKeyCode();

        if (code == KeyEvent.VK_W || code == KeyEvent.VK_S){
            myCam.cancelCamForward();
        }

        if (code == KeyEvent.VK_A || code == KeyEvent.VK_D){
            myCam.cancelCamLeft();
        }
        
        if (code == KeyEvent.VK_E || code == KeyEvent.VK_Q){
            myCam.cancelCamDown();
        }
        
        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_DOWN) {
        	myCam.cancelPitch();
        }
        
        if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_RIGHT) {
        	myCam.cancelPan();
        }
	}
}