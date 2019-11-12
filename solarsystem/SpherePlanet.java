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
 * SOURCES: Sphere code from Proj06_1a_CodedVersion from class references
 */

public class SpherePlanet extends Planet{

	private int my_vbo;
	
	private Sphere myPlanet;
	
	public SpherePlanet(GerstnerSystem sys, Vector3D position, String textureLoc, double orbitSpeed, double rotateSpeed) {
		super(sys,position,textureLoc,orbitSpeed,rotateSpeed);
		myPlanet = new Sphere(48);
		setupVerticesSphere();
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
	
	private void setupVerticesSphere()
	{
		Vertex3D[] vertices = myPlanet.getVertices();
		int[] indices = myPlanet.getIndices();
		
		float[] pvalues = new float[indices.length*3];
		float[] tvalues = new float[indices.length*2];
		float[] nvalues = new float[indices.length*3];
		
		for (int i=0; i<indices.length; i++)
		{	pvalues[i*3] = (float) (vertices[indices[i]]).getX();
			pvalues[i*3+1] = (float) (vertices[indices[i]]).getY();
			pvalues[i*3+2] = (float) (vertices[indices[i]]).getZ();
			tvalues[i*2] = (float) (vertices[indices[i]]).getS();
			tvalues[i*2+1] = (float) (vertices[indices[i]]).getT();
			nvalues[i*3] = (float) (vertices[indices[i]]).getNormalX();
			nvalues[i*3+1]= (float)(vertices[indices[i]]).getNormalY();
			nvalues[i*3+2]=(float) (vertices[indices[i]]).getNormalZ();
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
	
	static class Sphere
	{
		private int numVertices, numIndices, prec=48;
		private int[] indices;
		private Vertex3D[] vertices;
		
		public Sphere(int p)
		{
			prec = p;
			InitSphere();
		}
		
		private void InitSphere()
		{	
			numVertices = (prec+1) * (prec+1);
			numIndices = prec * prec * 6;
			vertices = new Vertex3D[numVertices];
			indices = new int[numIndices];

			for (int i=0; i<numVertices; i++) { vertices[i] = new Vertex3D(); }

			// calculate triangle vertices
			for (int i=0; i<=prec; i++)
			{	for (int j=0; j<=prec; j++)
				{	float y = (float)Math.cos(Math.toRadians(180-i*180/prec));
					float x = -(float)Math.cos(Math.toRadians(j*360.0/prec))*(float)Math.abs(Math.cos(Math.asin(y)));
					float z = (float)Math.sin(Math.toRadians(j*360.0f/(float)(prec)))*(float)Math.abs(Math.cos(Math.asin(y)));
					vertices[i*(prec+1)+j].setLocation(new Point3D(x,y,z));
					vertices[i*(prec+1)+j].setS((float)j/prec);
					vertices[i*(prec+1)+j].setT((float)i/prec);
					vertices[i*(prec+1)+j].setNormal(new Vector3D(vertices[i*(prec+1)+j].getLocation()));
			}	}
			
			// calculate triangle indices
			for(int i=0; i<prec; i++)
			{	for(int j=0; j<prec; j++)
				{	indices[6*(i*prec+j)+0] = i*(prec+1)+j;
					indices[6*(i*prec+j)+1] = i*(prec+1)+j+1;
					indices[6*(i*prec+j)+2] = (i+1)*(prec+1)+j;
					indices[6*(i*prec+j)+3] = i*(prec+1)+j+1;
					indices[6*(i*prec+j)+4] = (i+1)*(prec+1)+j+1;
					indices[6*(i*prec+j)+5] = (i+1)*(prec+1)+j;
		}	}	}

		public int[] getIndices()
		{	return indices;
		}

		public Vertex3D[] getVertices()
		{	return vertices;
		}
	}

}
