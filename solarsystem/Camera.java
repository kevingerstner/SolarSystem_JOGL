package solarsystem;

import graphicslib3D.Matrix3D;
import graphicslib3D.Vector3D;

/*
 * SOURCES: https://learnopengl.com/Getting-started/Camera
 */

public class Camera {
	private GerstnerSystem sys;
	
	private Vector3D cameraPos;
	private Vector3D camUp, camRight, camFwd;
	
	private Vector3D origin;
	private Vector3D up;
	
	private double stepAmt = 0.3;
	private double fwdSp = 0.0;
	private double rgtSp = 0.0;
	private double upSp = 0.0;
	
	private float yaw;
	private float pitch;
	
	private double degStepAmt = 1.0;
	private double pitchSp = 0.0;
	private double panSp = 0.0;
	
	public Camera(GerstnerSystem sys, Vector3D startPos) {
		this.sys = sys;
		cameraPos = startPos;
		
		camUp = new Vector3D(0.0,1.0,0.0);
		camRight = new Vector3D(1.0,0.0,0.0);
		camFwd = new Vector3D(0.0,0.0,1.0);
		
		origin = new Vector3D(0.0,0.0,0.0);
		up = new Vector3D(0.0f, 1.0f, 0.0f);
		
		yaw = -90.0f;
		pitch = 0.0f;
	}
	
	public void updateCamera() {
		//Try moving the camera
		cameraPos = cameraPos.add(camFwd.mult(stepAmt*fwdSp)); //move forward
		cameraPos = cameraPos.add(camRight.mult(stepAmt*rgtSp)); //move left
		cameraPos = cameraPos.add(camUp.mult(stepAmt*upSp)); //move down
		
		//update the camera pan (yaw)
		yaw += panSp * degStepAmt;
		
		//update the camera pitch
		if(pitch < 90.0){
			if(pitch > -90.0) {
				pitch += pitchSp * degStepAmt;
			} else {pitch = -90.0f;}
		} else{pitch = 90.0f;}
		
		//Update the fwd axis based on pitch and pan
		camFwd = new Vector3D(
                (Math.cos(Math.toRadians(pitch)) * Math.cos(Math.toRadians(yaw))),
                (Math.sin(Math.toRadians(pitch))),
                (Math.cos(Math.toRadians(pitch)) * Math.sin(Math.toRadians(yaw)))
				);
	}
	
	public void moveCam() {
		sys.mvStack.translate(-cameraPos.getX(), -cameraPos.getY(), -cameraPos.getZ());
	}
	
	//Calls the look at Matrix with correct inputs
	public Matrix3D lookOverHere() {
		Matrix3D look = lookAt(cameraPos,origin,up);
		return look;
	}
	
	/* Point3D eye = the position of the camera
	 * Point3D target = the point the camera is aimed at
	 * Vector3D y = the up direction  (typically {0,1,0} or the y-axis
	 */
	
	public Matrix3D lookAt(Vector3D eye, Vector3D target, Vector3D y) {
		
		Matrix3D look = new Matrix3D();
	
		//Use the camera axes in lookAt, relative to camFwd, which is updated above.
		camRight = (camFwd.cross(y)).normalize();
        camUp = (camRight.cross(camFwd)).normalize();
	
        look.setElementAt(0, 0, camRight.getX());
        look.setElementAt(1, 0, camUp.getX());
        look.setElementAt(2, 0, -camFwd.getX());
        look.setElementAt(3, 0, 0);
        look.setElementAt(0, 1, camRight.getY());
        look.setElementAt(1, 1, camUp.getY());
        look.setElementAt(2, 1, -camFwd.getY());
        look.setElementAt(3, 1, 0);
        look.setElementAt(0, 2, camRight.getZ());
        look.setElementAt(1, 2, camUp.getZ());
        look.setElementAt(2, 2, -camFwd.getZ());
        look.setElementAt(3, 2, 0);
        look.setElementAt(0, 3, camRight.dot(eye.mult(-1)));
        look.setElementAt(1, 3, camUp.dot(eye.mult(-1)));
        look.setElementAt(2, 3, (camFwd.mult(-1)).dot(eye.mult(-1)));
        look.setElementAt(3, 3, 1);
		return look;
	}
	
	//handle keyboard innput
	public void moveCamForward() {fwdSp = 1.0;}
	public void moveCamBackwards() {fwdSp = -1.0;}
	public void cancelCamForward() {fwdSp = 0.0;}
	
	public void moveCamLeft() {rgtSp = -1.0;}
	public void moveCamRight() {rgtSp = 1.0;}
	public void cancelCamLeft() {rgtSp = 0.0;}
	
	public void moveCamDown() {upSp = 1.0;}
	public void moveCamUp() {upSp = -1.0;}
	public void cancelCamDown() {upSp = 0.0;}
	
	public void pitchCamUp() {pitchSp = 1.0;}
	public void pitchCamDown() {pitchSp = -1.0;}
	public void cancelPitch() {pitchSp = 0.0;}
	
	public void panCamLeft() {panSp = -1.0;}
	public void panCamRight() {panSp = 1.0;}
	public void cancelPan() {panSp = 0.0;}
	
	Matrix3D perspective(float fovy, float aspect, float n, float f)
	{	float q = 1.0f / ((float) Math.tan(Math.toRadians(0.5f * fovy)));
		float A = q / aspect;
		float B = (n + f) / (n - f);
		float C = (2.0f * n * f) / (n - f);
		Matrix3D r = new Matrix3D();
		r.setElementAt(0,0,A);
		r.setElementAt(1,1,q);
		r.setElementAt(2,2,B);
		r.setElementAt(3,2,-1.0f);
		r.setElementAt(2,3,C);
		return r;
	}
	
	public Vector3D getCameraPos() {
		return cameraPos;
	}
	
	public Vector3D getCamAxisX() {
		return camRight;
	}
	
	public Vector3D getCamAxisY() {
		return camUp;
	}
	
	public Vector3D getCamAxisZ() {
		return camFwd;
	}

}
