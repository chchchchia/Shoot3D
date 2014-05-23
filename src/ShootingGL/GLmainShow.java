package ShootingGL;
import java.awt.Font;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.GLProfile;
import javax.media.opengl.glu.GLU;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.*;

import static javax.media.opengl.GL.*;  // GL constants
import static javax.media.opengl.GL2.*; // GL2 constants
import static javax.media.opengl.GL2ES1.GL_POINT_SMOOTH_HINT;

public class GLmainShow implements GLEventListener {
//The awt/swing Shooting, redone in JOGL with NEWT
	private static final int FPS = 60; // animator's target frames per second
	private static final int WINDOW_WIDTH = 750;
	private static final int WINDOW_HEIGHT = 520;
	private static String TITLE = "Shooting-GL";
	
	   private static final int MAX_PARTICLES = 100; // max number of particles
	   // Global speed for all the particles
	   private static float speedXGlobal = 0.0f;
	   private static float speedYGlobal = 0.0f;
	   private static float slowdown = 5.0f;//Used to slow down the ptcls
	   // Texture applied over the shape
	   private Texture texture;
	   private String textureFileName = "particle.png";
	   private String textureFileType = ".png";
	   // Texture image flips vertically. Shall use TextureCoords class to retrieve the
	   // top, bottom, left and right coordinates.
	   private float textureTop, textureBottom, textureLeft, textureRight;
	   // Pull forces in each direction
	   private static float forceX = 0.0f;
	   private static float forceY = -15.0f; // gravity
	   
//for texts
	   private TextRenderer textRenderer;
	   private String msg = "Active OpenGL Text With NeHe - ";
	   private DecimalFormat formatter = new DecimalFormat("##0.0");
	   private int textPosX; // x-position of the text
	   private int textPosY; // y-position of the text
	
	public static void main(String[] args) {
		 GLProfile glp = GLProfile.getDefault();
	        GLCapabilities caps = new GLCapabilities(glp);
//	        GLCanvas canvas = new GLCanvas(caps);
	        GLWindow window = GLWindow.create(caps);
	        final FPSAnimator animator = new FPSAnimator(window, FPS, true);

	        window.addWindowListener(new WindowAdapter() {
	            @Override
	            public void windowDestroyNotify(WindowEvent arg0) {
	               // Use a dedicate thread to run the stop() to ensure that the
	               // animator stops before program exits.
	               new Thread() {
	                  @Override
	                  public void run() {
	                     animator.stop(); // stop the animator loop
	                     System.exit(0);
	                  }
	               }.start();
	            };
	         });
	        GLmainShow glListener=new GLmainShow();
	        window.addGLEventListener(glListener);
	        window.addKeyListener(glListener.new KeyAdapter());
	        window.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
	        window.setTitle(TITLE);
	        window.setVisible(true);
	        animator.start();

	}
//declarations of so many of the vars
	public static final int TargetCount=1;
	public int TICK =30;
	public int TICKCOUNT=0;
	private int SHOTDELAY=5;
	private int MAXSHOTS=100;
	private int SHOTCOUNT=0;
	private boolean shotPresent=false;
	private boolean shotAllowed=true;
	private boolean left = false;
	private boolean right = true;
	private boolean up = false;
	private boolean down = false;
	private boolean rot_CW = false;
	private boolean rot_CCW = false;
	private int bulletStartY;
	//private BulletGL[] shots= new BulletGL[MAXSHOTS];
	private TurretGL gun;
	private Terrain terr=new Terrain();
	private Target[] target= new Target[TargetCount];
	//List<BulletGL> shots=new ArrayList<BulletGL>();
	//This should be fine performance wise as there are far more traversals of the array than mutations
	CopyOnWriteArrayList<BulletGL> shots=new CopyOnWriteArrayList<BulletGL>();
	//lines are [deg][point]
	Point[][] lines= new Point[360][];
	double[][][] lineEqnCoeffs;
	int currentHeading = 0;
	private int score=0;
	
	private GLU glu;
	
    @Override
    public void init(GLAutoDrawable drawable) {
    	  drawable.getGL().setSwapInterval(1);
    	  GL2 gl = drawable.getGL().getGL2();      // get the OpenGL graphics context
    	// Others: GL2GL3, GL2bc, GL4bc, GLES1, GLES2, GL2ES1, GL2ES2
	      glu = new GLU();                         // get GL Utilities
	      gl.glClearColor(1.0f, 1.0f, 1.0f, 0.0f); // set background (clear) color
	      gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST); // best perspective correction
	      gl.glShadeModel(GL_SMOOTH); // blends colors nicely, and smoothes out lighting
	      gl.glOrtho (0, WINDOW_WIDTH, 0,WINDOW_HEIGHT, 0, 100);
	      gl.glMatrixMode(GL_PROJECTION);
	      gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE); // type of blending to perform
	      gl.glHint(GL_POINT_SMOOTH_HINT, GL_NICEST); // really nice point smoothing
	      gl.glLoadIdentity();
	      
	      // Load the texture image
	      try {
	         // Create a OpenGL Texture object from (URL, mipmap, file suffix)
	         // Use URL so that can read from JAR and disk file.
	         texture = TextureIO.newTexture(
	               this.getClass().getResource(textureFileName), false, textureFileType);
	      } catch (GLException e) {
	         e.printStackTrace();
	      } catch (IOException e) {
	         e.printStackTrace();
	      }

	      // Use linear filter for texture if image is larger than the original texture
	      gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	      // Use linear filter for texture if image is smaller than the original texture
	      gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	      
	      // Texture image flips vertically. Shall use TextureCoords class to retrieve
	      // the top, bottom, left and right coordinates, instead of using 0.0f and 1.0f.
	      TextureCoords textureCoords = texture.getImageTexCoords();
	      textureTop = textureCoords.top();
	      textureBottom = textureCoords.bottom();
	      textureLeft = textureCoords.left();
	      textureRight = textureCoords.right();
	      
	      //initText
	      textRenderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 14));
	      Rectangle2D bounds = textRenderer.getBounds(msg + "0000.00");
	      int textWidth = (int)bounds.getWidth();
	      int textHeight = (int)bounds.getHeight();
	      textPosX = 0;
	      textPosY = drawable.getHeight()-textHeight;
	      
	      initTerrain();
	      initTurret();
	      initTarget();
    	}

    private void initTerrain(){
    	terr.loadPoints();
    	terr.CreateTransitions();
    	for(int deg=0;deg<360;deg++){
    		lines[deg]=terr.getLines(deg);
    	}
    	lineEqnCoeffs=terr.getLineCoeffs();
	}
    
	private void initTarget(){
		this.target[0]=new Target(600,115,25,25,0);
	}
	
	private void initTurret(){
		gun=new TurretGL(35,40);
	}
	
	
	 @Override
	    public void display(GLAutoDrawable drawable) {
	        update();
	        render(drawable);
     
	    }

	    @Override
	    public void dispose(GLAutoDrawable drawable) {
	    }


	    @Override
	    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
/*	    	GL2 gl = drawable.getGL().getGL2();  // get the OpenGL 2 graphics context
	        if (h == 0) h = 1;   // prevent divide by zero
	        float aspect = (float)w / h;
	   
	        // Set the view port (display area) to cover the entire window
	        gl.glViewport(0, 0, w, h);
	   
	        // Setup perspective projection, with aspect ratio matches viewport
	        gl.glMatrixMode(GL_PROJECTION);  // choose projection matrix
	        gl.glLoadIdentity();             // reset projection matrix
	        glu.gluPerspective(45.0, aspect, 0.1, 100.0); // fovy, aspect, zNear, zFar
	   
	        // Enable the model-view transform
	        gl.glMatrixMode(GL_MODELVIEW);
	        gl.glLoadIdentity(); // reset 
	        */
	    }
	    
	    private void update(){
	    	TICKCOUNT+=1;
	    	if(TICKCOUNT-TICK>=SHOTDELAY){
	    		shotAllowed=true;
	    	}
	        keypress();
	        moveShots();
	        checkCollisionLines();
	    }
	    
	    private void moveShots(){
	    	if (shotPresent){
//	    		for(int i=0;i<SHOTCOUNT;i++){	    		
//	    		for(BulletGL shot:shots){
	    		Iterator<BulletGL> iter=shots.iterator();
				while(iter.hasNext()){
					BulletGL shot=iter.next(); 
	    			if (!shot.isBooming()&&!shot.isBoomed()){
	    				shot.move(TICKCOUNT);
	    				System.out.println("shot coord=("+shot.getX()+","+shot.getY()+")");
	    			}
	    			if(shot.getX()>WINDOW_WIDTH){
	    				shot.setBoomed();
	    			}
	    		}
	    	}
		}
	    
	    private void render(GLAutoDrawable drawable){
	    	GL2 gl = drawable.getGL().getGL2();  // get the OpenGL 2 graphics context
	        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear color and depth buffer        
	        drawTerrain(gl);
	        drawTurret(gl);
	        drawShots(gl);
	        drawTarget(gl);
	        drawText(gl);
	    }
	    
	    private void drawText(GL2 gl){
	        textRenderer.beginRendering(WINDOW_WIDTH, WINDOW_HEIGHT);
	        textRenderer.setColor(0,0,0,1);                               
	        // 2D text using int (x, y) coordinates in OpenGL coordinates system,
	        // i.e., (0,0) at the bottom-left corner, instead of Java Graphics coordinates.
	        // x is set to between (+/-)10. y is set to be (+/-)80.
	        textRenderer.draw("Angle=" + formatter.format(gun.getAngle()), this.textPosX, this.textPosY);
	        textRenderer.draw("Power=" + formatter.format(gun.getPower()), this.textPosX, this.textPosY-textRenderer.getFont().getSize());
	        textRenderer.draw("Score=" + this.score, this.textPosX, this.textPosY-2*textRenderer.getFont().getSize());
	        textRenderer.draw("Shots=" + (this.MAXSHOTS-this.SHOTCOUNT), this.textPosX, this.textPosY-3*textRenderer.getFont().getSize());
	        textRenderer.draw("Heading=" + this.currentHeading, this.textPosX, this.textPosY-4*textRenderer.getFont().getSize());
	        textRenderer.endRendering();  // finish rendering
	    }
	    private void drawTarget(GL2 gl){
	    	for (int i=0;i<target.length;i++){
	    	if (target[i].isBooming()){
	    		target[i].drawBoom(gl, texture);
	    		System.out.println("target hit");
	    	} else if (!target[i].isDestroyed()&&(this.currentHeading<=target[i].getHeading()+5&&this.currentHeading>=target[i].getHeading()-5)){
	    		gl.glBegin(GL.GL_LINE_LOOP);
//	    		gl.glBegin(GL.GL_POLYGON_OFFSET_FILL);
	    		gl.glColor3f(0f, 0f,0f);
	    		gl.glVertex2f(target[i].getX(),target[i].getY());
	    		gl.glVertex2f(target[i].getX()+target[i].getWidth(),target[i].getY());
	    		gl.glVertex2f(target[i].getX()+target[i].getWidth(),target[i].getY()-target[i].getHeight());
	    		gl.glVertex2f(target[i].getX(),target[i].getY()-target[i].getHeight());
	    		gl.glEnd();
	    	}
	    	}
	    }
	    private void drawShots(GL2 gl) {
			if (shotPresent){
//				for(int i=0;i<SHOTCOUNT;i++){
				Iterator<BulletGL> iter=shots.iterator();
				while(iter.hasNext()){
					BulletGL shot=iter.next();
//				for(BulletGL shot:shots){
					if (!shot.isBoomed()&&shot.getHeading()==currentHeading){
						if (shot.isBooming()){
							if(!shot.ptclsActive){
								iter.remove();
							}else{
							drawBoom(gl,shots.indexOf(shot),0);
							}
						}else{
								float vx=shot.getVx();
								float vy=shot.getVy();
								float norm = (float) Math.sqrt((vx*vx)+(vy*vy));
								//ok, to get the shot to change with direction, we normalize the vx or vy to the mag of V, and let that
								//represent the fraction of the shot length, which is 5, because that number is pretty here. Yay pretty!
								gl.glBegin(GL.GL_LINES);
								gl.glColor3f(0f, 0f,0f);
								gl.glVertex2f(shot.getX(), shot.getY());
								gl.glVertex2f(shot.getX()+5*(vx/norm), (shot.getY()+5*(vy/norm)));
								gl.glEnd();
								
							}
						}		
				}
			}
		}
	    
	    private void drawBoom(GL2 gl, int k, int type){
	    	// Render the particles
	    	boolean ptclsActive=false;
	    	for(BulletParticle particle:shots.get(k).particles){
	    		if(particle.active){
	        	   ptclsActive=true;
	              // Draw the particle using RGB values, fade the particle based on it's life
	              gl.glColor4f(particle.r, particle.g, particle.b, particle.life);
	              texture.enable(gl);
	              texture.bind(gl);

	              gl.glBegin(GL_TRIANGLE_STRIP); // build quad from a triangle strip
	              float px = particle.x;
	              float py = particle.y;
	              float pz = particle.z;
	              
	              gl.glTexCoord2d(textureRight, textureTop);
	              gl.glVertex3f(px + 0.5f, py + 0.5f,pz); // Top Right
	              gl.glTexCoord2d(textureLeft, textureTop);
	              gl.glVertex3f(px - 0.5f, py + 0.5f,pz); // Top Left
	              gl.glTexCoord2d(textureRight, textureBottom);
	              gl.glVertex3f(px + 0.5f, py - 0.5f,pz); // Bottom Right
	              gl.glTexCoord2d(textureLeft, textureBottom);
	              gl.glVertex3f(px - 0.5f, py - 0.5f,pz); // Bottom Left
	              gl.glEnd();

	              // Move the particle
	              particle.x += particle.speedX / (slowdown * 1000.0f);
	              particle.y += particle.speedY / (slowdown * 1000.0f);

	              // Apply gravity
	              particle.speedX += forceX;
	              particle.speedY += forceY;

	              // Take away some life
	              particle.life -= particle.fade;
	              if (particle.life < 0.0f) {  // check for burst also
	            	  particle.inactivate();
	              }
	           }
	        }
	        if (!ptclsActive){
	        	if(type==0){
//	        	shots[k].setKasploded();
	        	shots.get(k).setBoomed();
	        	}else if(type==1){
	        		target[0].setDestroyed();
	        		System.out.println("Target destroyed");
	        	}
	        }
	    }
	    
	    private void drawTerrain(GL2 gl){
	    	gl.glBegin(GL.GL_LINES);
	    	gl.glColor3f(0f, 0f,0f);
			for (int i=0;i<terr.getLines(currentHeading).length-1;i++){
				gl.glVertex2f((float)lines[currentHeading][i].getX(),(float)lines[currentHeading][i].getY());
				gl.glVertex2f((float)lines[currentHeading][i+1].getX(), (float)lines[currentHeading][i+1].getY());
			}
			gl.glEnd();
		}
	    
	    private void drawTurret(GL2 gl){
	    	   gl.glBegin(GL_LINE_LOOP);
	    	   float radius = gun.getRadius()/2;
	    	   float X = gun.getX();
	    	   float Y = gun.getY();
	    	   for (float i=0; i<Math.PI; i+=0.0175){
	    	      gl.glVertex2d(X+Math.cos(i)*radius,Y+Math.sin(i)*radius);
	    	   }    	 
	    	   gl.glEnd();	    	
	    	   float arcX=gun.getX()-gun.getRadius()/2;
	    	   float arcY=gun.getY()-gun.getRadius()/2;

			double x1=(arcX+gun.getRadius()/2)+(gun.getRadius()/2)*Math.cos(gun.getAngle()*Math.PI/180);
			double y1=(arcY+gun.getRadius()/2)+(gun.getRadius()/2)*Math.sin(gun.getAngle()*Math.PI/180);
			double x2=(arcX+gun.getRadius()/2)+(gun.getBarrelLength())*Math.cos(gun.getAngle()*Math.PI/180);
			double y2=(arcY+gun.getRadius()/2)+(gun.getBarrelLength())*Math.sin(gun.getAngle()*Math.PI/180);
			bulletStartY=(int)y2+gun.getRadius()/4;		
			gl.glBegin(GL.GL_LINES);
			gl.glColor3f(0f,0f,0f);
			gl.glVertex2d(x1, y1);
			gl.glVertex2d(x2, y2);
			gl.glEnd();
		}
	    
	    private void keypress(){
	    	if (left==true){
	    		gun.aim(1);
	    		left=false;
	    	}else if (right==true){
	    		gun.aim(-1);
	    		right=false;
	    	}
	    	if (up==true){
	    		gun.power(1);
	    		up=false;
	    	}else if (down==true){
	    		gun.power(-1);
	    		down=false;
	    	}
	    	if (rot_CW){
	    		rotCW();
	    		rot_CW=false;
	    	}else if (rot_CCW){
	    		rotCCW();
	    		rot_CCW=false;
	    	}
	    }
	    
	    public void rotCW(){
	    	this.currentHeading+=1;
	    	if (currentHeading==360){
	    		this.currentHeading=0;
	    	}
	    	updateTerrain();
	    }
	    
	    public void rotCCW(){
	    	this.currentHeading-=1;
	    	if (currentHeading<0){
	    		this.currentHeading=this.currentHeading+360;
	    	}
	    	updateTerrain();
	    }
	    
	    private void updateTerrain(){
	    	System.out.println("degrees="+this.currentHeading);
	    	//TODO this is empty
	    }
	    
	    public void checkCollisionLines(){
	    	//This method calculates a line based on the line seg the shot is currently over and sees if the shot has fallen below that line
			int buffer=0;
			//buffer is the fuzzy region around a line	
//			for (int j=0;j<SHOTCOUNT;j++){
			if(!shots.isEmpty()){
				Iterator<BulletGL> iter=shots.iterator();
//			for(BulletGL shot:shots){
				//TODO code to destroy user's gun if struck by a shot
			while(iter.hasNext()){
				BulletGL shot=iter.next(); 
				for (int i=0;i<terr.getLines(shot.getHeading()).length-1;i++){
					if(!shot.isBooming()&&!shot.isBoomed()){
					int xm=shot.getX();
					int yp=shot.getY();
					float x1=(float)lines[shot.getHeading()][i].getX();
					float x2=(float)lines[shot.getHeading()][i+1].getX();
					float y1=(float)lines[shot.getHeading()][i].getY();
					float y2=(float)lines[shot.getHeading()][i+1].getY();
					System.out.println("x1="+x1+" x2="+x2+" shotX="+shot.getX());
					if (intervallContains(x1,x2,xm+buffer)||intervallContains(x1,x2,xm-buffer)){
						System.out.println("x check with x1="+x1+" x2="+x2+" shotX="+shot.getX());
						System.out.println("y is y1="+y1+" y2="+y2+" shotY="+shot.getY());
						if(intervallContains(y1,y2,yp+5)||intervallContains(y1,y2,yp-5)||y1==y2||(yp<y1&&yp<y2)){
							//calculate a line eqn, see if ptcl is below it
							//Uses precalc'd line coeffs to see if point is below terrain line
							//division is slower than straight multiplication, but on this system and this app, it's ok
							float lineY=(y2-y1)/(x2-x1)*(xm-x1)+y1;
//The non-division way		if((x2-x1)*yp<=(y2-y1)*(xm-x1)+y1*(x2-x1)){
							if(yp<=lineY){
								for(Target t:target){
									if(intervallContains(t.getX(),t.getX()+t.getWidth(),xm)&&intervallContains(t.getHeading()-5,t.getHeading()+5,shot.getHeading())
											&&!t.isDestroyed()){
										shot.setBoomed();
										t.initPtcls(t.getX()+t.getWidth()/2, t.getY()-t.getHeight()/2);
										t.initPtcls(t.getX()+t.getWidth(), t.getY()-5);
										t.setHit();
										this.score++;
									}
								}
								shot.boom();
								shot.initPtcls(shot.getX(), lineY);
								System.out.println("shot boom x,y="+xm+","+yp);
								System.out.println("x1="+x1+" x2="+x2+" y1="+y1+" y2="+y2+" i="+i);
								}   
							}
							if (y1==y2&&yp<y1){
								shot.boom();
								shot.initPtcls(shot.getX()-1, y1+1);
							}
						}
					//OK Now that we've checked the terrain, check to see if we've hit the target
					for(int t=0;t<target.length;t++){
						if (!target[t].isBooming()&&!target[t].isDestroyed()&&
								intervallContains(target[t].getHeading()-5,target[t].getHeading()+5,shot.getHeading())){
							int xT=target[t].getX();
							int yT=target[t].getY();
							if(intervallContains(xT,xT+target[t].getWidth(),xm)){
						if(intervallContains(yT-target[t].getHeight(),yT,yp)){
							shot.setBoomed();
							target[t].initPtcls(target[t].getX()+target[t].getWidth()/2, target[t].getY()-target[t].getHeight()/2);
				//			target[t].initPtcls(target[t].getX()+target[t].getWidth(), target[t].getY()-5);
							target[t].setHit();
							this.score++;
						}
					}
					}
					}
					}
								
				if (shot.ptclsActive){
					//Collision check for ptcls
					for (int l = 0; l < shot.MAX_PARTICLES; l++) {
						if(shot.particles[l].active){
						float xp=shot.particles[l].getX();
						float yp=shot.particles[l].getY();
						float x1=(float)lines[shot.getHeading()][i].getX();
						float x2=(float)lines[shot.getHeading()][i+1].getX();
						float y1=(float)lines[shot.getHeading()][i].getY();
						float y2=(float)lines[shot.getHeading()][i+1].getY();
						if (intervallContains(x1,x2,xp+buffer)||intervallContains(x1,x2,xp-buffer)){
							if(intervallContains(y1,y2,yp+buffer)||intervallContains(y1,y2,yp-buffer)||y1==y2){
								if (y1==y2){
									shot.particles[l].speedY=Math.abs(shot.particles[l].speedY*0.999f);
								}else{
									if((x2-x1)*yp<=(y2-y1)*(xp-x1)+y1*(x2-x1)){
									shot.particles[l].setY(yp+3);	
									shot.particles[l].speedX=-shot.particles[l].speedX*0.7f;
									shot.particles[l].speedY=-shot.particles[l].speedY*0.7f;
									}
								}
								}
							}
						//OK Now that we've checked the terrain, check to see if we've hit the target
						for(int t=0;t<target.length;t++){
							if (!target[t].isBooming()&&!target[t].isDestroyed()&&target[t].getHeading()==shot.getHeading()){
								int xT=target[t].getX();
								int yT=target[t].getY();
								if(intervallContains(xT,xT+target[t].getWidth(),xp)){
									if(intervallContains(yT-target[t].getHeight(),yT,yp)){										
										shot.particles[l].speedX=-1*shot.particles[l].speedX*0.7f;
								
									}
								}
							}
						}
						}
					}
				}
				}
				} 
			}
	    }
/*	    
	    public void checkCollision() {
	    	//This method is best for non point like objects, like spheres
			//code for collision with terrain, target
			//needs to destruct shots, reduce shotcount if they his something
			//TODO code for blowing up the user's gun
			int shotRadius=3;
			int buffer=5;
			//buffer is the fuzzy region around a line	
			for (int i=0;i<lines.length-1;i++){
				for (int j=0;j<SHOTCOUNT;j++){
					//xp=xPlus Radius, xm=x minus radius, same for y
					//xC=x of center of shot, yC=y of center of shot
					if(!shots[j].isBooming()&&!shots[j].isBoomed()){
					int xp=shots[j].getX()+2*shotRadius;
					int xC=xp-shotRadius;
					int xm=shots[j].getX();
					int yp=shots[j].getY();
					int ym=shots[j].getY()-2*shotRadius;
					int yC=ym+shotRadius;
					int x1=lines[i][0];
					int x2=lines[i+1][0];
					int y1=lines[i][1];
					int y2=lines[i+1][1];
					if (intervallContains(x1,x2,xp+buffer)||intervallContains(x1,x2,xm-buffer)){
						if(intervallContains(y1,y2,yp+buffer)||intervallContains(y1,y2,ym-buffer)||y1==y2){
							double hsqd=Math.pow(x2-xC,2)+Math.pow(y2-yC,2);
							double lsqd=Math.pow(x2-x1,2)+Math.pow(y2-y1,2);
							double dsqd=Math.pow(xC-x1,2)+Math.pow(yC-y1,2);
							double h=Math.sqrt(hsqd);
							double l=Math.sqrt(lsqd);
							double theta=Math.acos((hsqd+lsqd-dsqd)/(2*h*l));
							double k = h*Math.sin(theta);
							if(k<=shotRadius){
								shots[j].boom();
								shots[j].initPtcls(shots[j].getX(), shots[j].getY());
								System.out.println("shot x,y="+xm+","+yp);
								System.out.println("k radius="+k+" h="+h+"theta= "+theta+" l=" +l);
								}
							}if (y1==y2&&ym<y1){
								shots[j].boom();
								shots[j].initPtcls(shots[j].getX(), shots[j].getY()+2);
//								shots[j].initPtcls(shots[j].getX(), y1+2);
							}
						}
					//OK Now that we've checked the terrain, check to see if we've hit the target
					if (!target.isBooming()&&!target.isDestroyed()){
					int xT=target.getX();
					int yT=target.getY();
					if(intervallContains(xT,xT+target.getWidth(),xp)||intervallContains(xT,xT+target.getWidth(),xm)){
						if(intervallContains(yT,yT+target.getHeight(),yp)||intervallContains(yT,yT+target.getHeight(),ym)){
							shots[j].setKasploded();
//							shots[j].initPtcls(shots[j].getX(), WINDOW_HEIGHT-shots[j].getY());
							target.initPtcls(target.getX()+target.getWidth()/2, target.getY()-target.getHeight()/2);
							target.setHit();
							this.score++;
						}
					}
					}
					}
					
					
				
				if (shots[j].ptclsActive){
					//Collision check for ptcls
//					System.out.println("ptclColCheck");
					for (int l = 0; l < shots[j].MAX_PARTICLES; l++) {
						//xp=xPlus Radius, xm=x minus radius, same for y
						//xC=x of center of shot, yC=y of center of shot
						if(shots[j].particles[l].active){
						float xp=shots[j].particles[l].getX();
						float xC=xp;
						float xm=shots[j].particles[l].getX();
						float yp=shots[j].particles[l].getY();
						float ym=shots[j].particles[l].getY();
						float yC=ym;
						float x1=lines[i][0];
						float x2=lines[i+1][0];
						float y1=lines[i][1];
						float y2=lines[i+1][1];
						if (intervallContains(x1,x2,xp+buffer)||intervallContains(x1,x2,xm-buffer)){
							if(intervallContains(y1,y2,yp+buffer)||intervallContains(y1,y2,ym-buffer)||y1==y2){
								if (y1==y2&&yp<=y1){
									shots[j].particles[l].speedY=Math.abs(shots[j].particles[l].speedY*0.999f);
									shots[j].particles[l].setY(y1+2);
									System.out.println("ptclColCheck1 yp="+yp);
								}else{
									if((x2-x1)*yp<=(y2-y1)*(xm-x1)+y1*(x2-x1)){
										shots[j].particles[l].setY(y1+1);
									shots[j].particles[l].speedX=-shots[j].particles[l].speedX*0.7f;
									shots[j].particles[l].speedY=-shots[j].particles[l].speedY*0.7f;
									System.out.println("ptclColCheck");
									}
								}
								}
							}
						//OK Now that we've checked the terrain, check to see if we've hit the target
						if (!target.isBooming()&&!target.isDestroyed()){
						int xT=target.getX();
						int yT=target.getY();
						if(intervallContains(xT,xT+target.getWidth(),xp)||intervallContains(xT,xT+target.getWidth(),xm)){
							if(intervallContains(yT,yT+target.getHeight(),yp)||intervallContains(yT,yT+target.getHeight(),ym)){
								shots[j].particles[l].speedX=-shots[j].particles[l].speedX*0.7f;
								
							}
						}
						}
						}
					}
				}
				}
				} 
			} */
	    //TODO make this private
	    public static boolean intervallContains(float low, float high, float n) {
			if (n >= low && n <= high){
				return true;
			}else if(n >= high && n <= low){
				return true;
			}else{
				return false;
			}
		}
	    
	    class Particle {
	        boolean active=true; // always active in this program
	        float life;     // life time
	        float fade;     // fading speed, which reduces the life time
	        float r, g, b;  // color
	        float x, y, z;  // position
	        float speedX, speedY, speedZ; // speed in the direction
	        private Random rand = new Random();

	        public Particle(float x, float y) {
	           boolean active = true;
	           this.x=x;
	           this.y=y;
	           this.z=0;
	           life = 1.0f;
	           this.burst();
	        }
	        public float getX(){
	        	return this.x;
	        }
	        public float getY(){
	        	return this.y;
	        }
	        public float getZ(){
	        	return this.z;
	        }
	        public void setX(float x){
	        	this.x=x;
	        }
	        public void setY(float y){
	        	this.y=y;
	        }
	        public void setZ(float z){
	        	this.z=z;
	        }
	        public void setLife(float life){
	        	life=this.life;
	        }
	        public void inactivate(){
	        	this.active=false;
	        }
	        public void burst() {
//	           life = 1.0f;

	           // Set a random fade speed value between 0.003 and 0.103
	           fade = rand.nextInt(100) / 1000.0f + 0.03f;

	           // Set the initial position
//	           x = y = z = 0.0f;
	           
	           // Generate a random speed and direction in polar coordinate, then resolve
	           // them into x and y.
	           // Set Random speed between -25 to +25
	           float speed = (rand.nextInt(50) - 25.0f);
	           float angle = (float)Math.toRadians(rand.nextInt(360));

	           // Multiplied by 10 to create a spectacular explosion when the program first starts
	           speedX = speed * (float)Math.cos(angle) * 100.0f;
	           speedY = speed * (float)Math.sin(angle) * 100.0f;
	           speedZ=0;
	           
	           r=0;
	           g=0;
	           b=0;
	        }

	        public void regenerate() {
	           life = 1.0f;
	           fade = rand.nextInt(100) / 1000.0f + 0.003f;
	           x = y = z = 0.0f;

	           // Generate a random speed and direction in polar coordinate, then resolve
	           // them into x and y. Increase the Random speed to between -30 to +30
	           float speed = (rand.nextInt(60) - 30.0f);
	           float angle = (float)Math.toRadians(rand.nextInt(360));

	           // Not multiply by 10 for subsequent launch
	           speedX = speed * (float)Math.cos(angle) + speedXGlobal;
	           speedY = speed * (float)Math.sin(angle) + speedYGlobal;
	           speedZ = rand.nextInt(60) - 30.0f;

	           // Use the current color
	           r = 1;
	           g = 1;
	           b = 0;
	        }
	     }
	    private class KeyAdapter implements KeyListener {
	    	@Override
	        public void keyPressed(com.jogamp.newt.event.KeyEvent e) {

	            int key = e.getKeyCode();

	            if ((key == com.jogamp.newt.event.KeyEvent.VK_LEFT) && (!right)) {
	                left = true;
	                up = false;
	                down = false;
	            }

	            if ((key == com.jogamp.newt.event.KeyEvent.VK_RIGHT) && (!left)) {
	                right = true;
	                up = false;
	                down = false;
	            }

	            if ((key == com.jogamp.newt.event.KeyEvent.VK_UP) && (!down)) {
	                up = true;
	                right = false;
	                left = false;
	            }

	            if ((key == com.jogamp.newt.event.KeyEvent.VK_DOWN) && (!up)) {
	                down = true;
	                right = false;
	                left = false;
	            }
	            
	            if ((key == com.jogamp.newt.event.KeyEvent.VK_M)) {
	                rot_CW=true;
	            }
	            
	            if ((key == com.jogamp.newt.event.KeyEvent.VK_N)) {
	                rot_CCW=true;
	            }
	            
	            
	            if ((key==com.jogamp.newt.event.KeyEvent.VK_SPACE)){
	            	if(SHOTCOUNT<MAXSHOTS&&shotAllowed){
	            	//shots[SHOTCOUNT]=new BulletGL(gun.getX()+(int)((gun.getBarrelLength())*Math.cos(gun.getAngle()*Math.PI/180)),bulletStartY,gun.getPower(),gun.getAngle(),TICKCOUNT,currentHeading);
	            	shots.add(new BulletGL(gun.getX()+(int)((gun.getBarrelLength())*Math.cos(gun.getAngle()*Math.PI/180)),bulletStartY,gun.getPower(),gun.getAngle(),TICKCOUNT,currentHeading));
	            	SHOTCOUNT++;
	            	shotPresent=true;
	            	shotAllowed=false;
	            	TICK=TICKCOUNT;
	            	System.out.println("SHOT");
	            	}
	            }
	        }


			@Override
			public void keyReleased(com.jogamp.newt.event.KeyEvent e) {
			}
	    }
}
