package solarsystem;

import java.nio.FloatBuffer;
import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_CCW;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_LEQUAL;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.util.texture.Texture;

import graphicslib3D.Matrix3D;
import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;
import graphicslib3D.Vertex3D;

/*
 * SOURCES: http://mathworld.wolfram.com/RegularPentagon.html
 * Equations for Pentagon points found on Wolfram Alpha
 */

public class PentagonalPlanet extends Planet{

	private int my_vbo;
	
	private PentagonalPrism myPlanet;
	
	public PentagonalPlanet(GerstnerSystem sys, Vector3D position, String textureLoc, double orbitSpeed, double rotateSpeed) {
		super(sys,position,textureLoc,orbitSpeed,rotateSpeed);
		myPlanet = new PentagonalPrism();
		setupVerticesPentagon();
	}
	
	public void DisplayPlanet() {
		int mv_loc = gl.glGetUniformLocation(sys.rendering_program, "mv_matrix");
		
		gl.glUniformMatrix4fv(mv_loc, 1, false, sys.mvStack.peek().getFloatValues(), 0); //Draw rotated Azalea
		//Enable Position Buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, sys.vbo[my_vbo]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		//Enable Texture Coordinates
		gl.glBindBuffer(GL_ARRAY_BUFFER, sys.vbo[my_vbo+1]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		//Activate Texture Unit, Bind to texture
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, tex);
		
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		
		int numVerts = getIndices().length;
		gl.glDrawArrays(GL_TRIANGLES, 0, numVerts);
	}
	
	private void setupVerticesPentagon()
	{
		Vertex3D[] vertices = myPlanet.getVertices();
		int[] indices = myPlanet.getIndices();
		
		float[] pvalues = new float[indices.length*3];
		float[] tvalues = new float[indices.length*2];
		float[] nvalues = new float[indices.length*3];
		
		for (int i=0; i<indices.length; i++){	
			pvalues[i*3] = (float) (vertices[indices[i]]).getX();
			pvalues[i*3+1] = (float) (vertices[indices[i]]).getY();
			pvalues[i*3+2] = (float) (vertices[indices[i]]).getZ();
			tvalues[i*2] = (float) (vertices[indices[i]]).getS();
			tvalues[i*2+1] = (float) (vertices[indices[i]]).getT();
			//nvalues[i*3] = (float) (vertices[indices[i]]).getNormalX();
			//nvalues[i*3+1]= (float)(vertices[indices[i]]).getNormalY();
			//nvalues[i*3+2]=(float) (vertices[indices[i]]).getNormalZ();
		}
		
		my_vbo = sys.nextvbo;
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, sys.vbo[sys.nextvbo]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);
		sys.nextvbo++;

		gl.glBindBuffer(GL_ARRAY_BUFFER, sys.vbo[sys.nextvbo]);
		FloatBuffer texBuf = Buffers.newDirectFloatBuffer(tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()*4, texBuf, GL_STATIC_DRAW);
		sys.nextvbo++;

		gl.glBindBuffer(GL_ARRAY_BUFFER, sys.vbo[sys.nextvbo]);
		FloatBuffer norBuf = Buffers.newDirectFloatBuffer(nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit()*4,norBuf, GL_STATIC_DRAW);
		sys.nextvbo++;
	}

	public Vertex3D[] getVertices() {
		return myPlanet.getVertices();
	}
	
	public int[] getIndices() {
		return myPlanet.getIndices();
	}
	
	static class PentagonalPrism
	{
		private int numVertices, numIndices, prec=48;
		private int[] indices;
		private Vertex3D[] vertices;
		
		public PentagonalPrism()
		{	
			InitPentagon();
		}
		
		private void InitPentagon()
		{	numVertices = 10;
			numIndices = (9 * 2)+(6 * 5); //front faces need 9, each side needs 6
			vertices = new Vertex3D[numVertices];
			double length = 0.75;

			for (int i=0; i<numVertices; i++) { vertices[i] = new Vertex3D(); }
			
			//The shift in y from (0,0)
			double c1 = Math.cos((2*Math.PI)/5);
			double c2 = Math.cos((Math.PI)/5);
			
			//The shift in x from (0,0)
			double s1 = Math.sin((2*Math.PI)/5);
			double s2 = Math.sin((4*Math.PI)/5);
			
			double x=0.0, y=0.0, z=0.0;
			
			for(int i=0;i<numVertices;i++) {
				switch (i % 5) {
					case 0:
						x = 0;
						y = 1;
						break;
					case 1:
						x = -s1;
						y = c1;
						break;
					case 2:
						x = -s2;
						y = -c2;
						break;
					case 3:
						x = s2;
						y = -c2;
						break;
					case 4:
						x = s1;
						y = c1;
						break;
					default:
						System.out.println("I just did a bad thing.");
				}
				if(0 <= i && i <= 4) {z = length;}
				else {z = -length;}
				
				vertices[i].setLocation(new Point3D(x,y,z));
			}
			
			vertices[0].setS(0.5); vertices[0].setT(1.0);
			vertices[1].setS(0.0); vertices[1].setT(0.6);
			vertices[2].setS(0.3); vertices[2].setT(0.0);
			vertices[3].setS(0.7); vertices[3].setT(0.0);
			vertices[4].setS(1.0); vertices[4].setT(0.6);
			vertices[5].setS(0.5); vertices[5].setT(1.0);
			vertices[6].setS(0.0); vertices[6].setT(0.6);
			vertices[7].setS(0.3); vertices[7].setT(0.0);
			vertices[8].setS(0.7); vertices[8].setT(0.0);
			vertices[9].setS(1.0); vertices[9].setT(0.6);
		
			
			int[] index = { 0,3,4,	0,2,3,	0,1,2, //front face
							5,8,9,	5,7,8,	5,6,7, //back face
							0,5,6,	0,1,6,			//one side
							1,6,7,	1,2,7,
							2,7,8,	2,3,8,
							3,8,9,	3,4,9,
							4,9,5,	4,0,5,
			};
			indices = index;
			
		}

		public int[] getIndices()
		{	return indices;
		}

		public Vertex3D[] getVertices()
		{	return vertices;
		}
	}

}