import java.awt.Font;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.GLProfile;
import javax.media.opengl.glu.GLU;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Random;

import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import com.jogamp.opengl.util.texture.TextureIO;

import static javax.media.opengl.GL.*;  // GL constants
import static javax.media.opengl.GL2.*; // GL2 constants
import static javax.media.opengl.GL2ES1.GL_POINT_SMOOTH_HINT;

public class GLmainShow implements GLEventListener {

	
//The awt/swing Shooting, redone in JOGL with NEWT
	
	private static final int FPS = 60; // animator's target frames per second
	private static final int WINDOW_WIDTH = 750;
	private static final int WINDOW_HEIGHT = 520;
	private static String TITLE = "Shooting-GL";
	
//for the boom effect
	   private static final int MAX_PARTICLES = 100; // max number of particles
	   // Global speed for all the particles
	   private static float speedXGlobal = 0.0f;
	   private static float speedYGlobal = 0.0f;
	   private static float slowdown = 5.0f;//Used to slow down the ptcls
	   // Texture applied over the shape
	   //TODO this is straight up stolen from nehe's tutorial, change it to something unique
	   private Texture texture;
	   private String textureFileName = "images/particle.png";
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
//declarations of so manys of the vars
	public int TICK =30;
	public int TICKCOUNT=0;
	private int MAXSHOTS=100;
	private int SHOTCOUNT=0;
	private boolean shotPresent=false;
	private boolean left = false;
	private boolean right = true;
	private boolean up = false;
	private boolean down = false;
	private int bulletStartY;
	private BulletGL[] shots= new BulletGL[MAXSHOTS];
	private TurretGL gun;
	private Terrain terr=new Terrain();
	private Target target;
	int[][] lines;
//	private Particle[] particles = new Particle[MAX_PARTICLES];
	private int score=0;
	
	private GLU glu;  // for the GL Utility
	
    @Override
    public void init(GLAutoDrawable drawable) {
    	  drawable.getGL().setSwapInterval(1);
    	  GL2 gl = drawable.getGL().getGL2();      // get the OpenGL graphics context
    	// Others: GL2GL3, GL2bc, GL4bc, GLES1, GLES2, GL2ES1, GL2ES2
	      glu = new GLU();                         // get GL Utilities
	      gl.glClearColor(1.0f, 1.0f, 1.0f, 0.0f); // set background (clear) color
//	      gl.glClearDepth(1.0f);      // set clear depth value to farthest
//	      gl.glEnable(GL_DEPTH_TEST); // enables depth testing
//	      gl.glDepthFunc(GL_LEQUAL);  // the type of depth test to do
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
	    // Allocate textRenderer with the chosen font
	      textRenderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 14));

	      Rectangle2D bounds = textRenderer.getBounds(msg + "0000.00");
	      int textWidth = (int)bounds.getWidth();
	      int textHeight = (int)bounds.getHeight();
	        // Centralize text on the canvas
	      textPosX = 0;
	      textPosY = drawable.getHeight()-textHeight;
	      
	      initTerrain();
	      initTurret();
	      initTarget();
    	}
    
	
 
/*    private void initPtcls(float x, float y, int type){
    	  // Initialize the particles
    	if (type==0){
	      for (int i = 0; i < MAX_PARTICLES; i++) {
	         particles[i] = new Particle(x,y);
//	         particles[i].setX(x);
//             particles[i].setY(y);
	      }
    	}else if (type==1){
    		//for target explosion, change things here
    		for (int i = 0; i < MAX_PARTICLES; i++) {
   	         particles[i] = new Particle(x,y);
    		}
    	}
    }
    */
    private void initTerrain(){
		this.lines=terr.getLines();
		
	}
    
	private void initTarget(){
		this.target=new Target(600,115,25,25);
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
	        keypress();
	        moveShots();
	        checkCollision();
	    }
	    
	    private void moveShots(){
			//for loop here, eventually, for an array of bullets
	    	if (shotPresent){
	    		for(int i=0;i<SHOTCOUNT;i++){
	    			if (!shots[i].isBooming()&&!shots[i].isBoomed()){
	    				shots[i].move(TICKCOUNT);
//	    				System.out.println("MOVE");
	    			}
	    		}
	    	}
		}
	    
	    private void render(GLAutoDrawable drawable){
	    	GL2 gl = drawable.getGL().getGL2();  // get the OpenGL 2 graphics context
	        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear color and depth buffers
	        
//	        gl.glBegin(GL.GL_LINES);
//	        gl.glVertex2f(10, 50);
//	        gl.glVertex2f(500, 250);
//	        gl.glColor3f(0f, 0f,0f);
//	        gl.glEnd();
	        
	        drawTerrain(gl);
	        drawTurret(gl);
	        drawShots(gl);
	        drawTarget(gl);
	        drawText(gl);
	    }
	    
	    private void drawText(GL2 gl){
	    	 // Prepare to draw text
	        textRenderer.beginRendering(WINDOW_WIDTH, WINDOW_HEIGHT);

	        // Pulsing colors based on text position, set color in RGBA
	        textRenderer.setColor(0,0,0,1);                               

	        // 2D text using int (x, y) coordinates in OpenGL coordinates system,
	        // i.e., (0,0) at the bottom-left corner, instead of Java Graphics coordinates.
	        // x is set to between (+/-)10. y is set to be (+/-)80.
	        textRenderer.draw("Angle=" + formatter.format(gun.getAngle()), this.textPosX, this.textPosY);
	        textRenderer.draw("Power=" + formatter.format(gun.getPower()), this.textPosX, this.textPosY-textRenderer.getFont().getSize());
	        textRenderer.draw("Score=" + this.score, this.textPosX, this.textPosY-2*textRenderer.getFont().getSize());
	        textRenderer.draw("Shots=" + (this.MAXSHOTS-this.SHOTCOUNT), this.textPosX, this.textPosY-3*textRenderer.getFont().getSize());
	        textRenderer.endRendering();  // finish rendering
	    }
	    private void drawTarget(GL2 gl){
	    	if (target.isBooming()){
	    	target.drawBoom(gl, texture);
	    	System.out.println("target hit");
	    	} else if (target.isDestroyed()){
	    		
	    	}else{
	    	gl.glBegin(GL.GL_LINE_LOOP);
	    	gl.glColor3f(0f, 0f,0f);
	    	gl.glVertex2f(target.getX(),target.getY());
	    	gl.glVertex2f(target.getX()+target.getWidth(),target.getY());
	    	gl.glVertex2f(target.getX()+target.getWidth(),target.getY()-target.getHeight());
	    	gl.glVertex2f(target.getX(),target.getY()-target.getHeight());
	    	gl.glEnd();
	    	}
	    }
	    private void drawShots(GL2 gl) {
			if (shotPresent){
				for(int i=0;i<SHOTCOUNT;i++){
					if (!shots[i].isBoomed()){
						if (shots[i].isBooming()){
							//NOTE: not adjusting the x and y by boomRad/2 makes the circle expand downward, like it's raining boom
							drawBoom(gl,i,0);
						}else{
								float vx=shots[i].getVx();
								float vy=shots[i].getVy();
								float norm = (float) Math.sqrt((vx*vx)+(vy*vy));
								//ok, to get the shot to change with direction, we normalize the vx or vy to the mag of V, and let that
								//represent the fraction of the shot length, which is 5, because that number is pretty here
								gl.glBegin(GL.GL_LINES);
								gl.glColor3f(0f, 0f,0f);
								gl.glVertex2f(shots[i].getX(), shots[i].getY());
								gl.glVertex2f(shots[i].getX()+5*(vx/norm), (shots[i].getY()+5*(vy/norm)));
								gl.glEnd();
								
							}
						}		
				}
			}
		}
	    
	    private void drawBoom(GL2 gl, int k, int type){
	    	// Render the particles
	    	boolean ptclsActive=false;
	        for (int i = 0; i < shots[k].MAX_PARTICLES; i++) {
	           if (shots[k].particles[i].active) {
	        	   ptclsActive=true;
	              // Draw the particle using our RGB values, fade the particle based on it's life
	              gl.glColor4f(shots[k].particles[i].r, shots[k].particles[i].g, shots[k].particles[i].b, shots[k].particles[i].life);
	              texture.enable(gl);
	              texture.bind(gl);

	              gl.glBegin(GL_TRIANGLE_STRIP); // build quad from a triangle strip
	              float px = shots[k].particles[i].x;
	              float py = shots[k].particles[i].y;
	              float pz = shots[k].particles[i].z;
	              
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
	              shots[k].particles[i].x += shots[k].particles[i].speedX / (slowdown * 1000.0f);
	              shots[k].particles[i].y += shots[k].particles[i].speedY / (slowdown * 1000.0f);
//	              shots[k].particles[i].z += shots[k].particles[i].speedZ / (slowdown * 1000.0f);

	              // Apply the gravity force
	              shots[k].particles[i].speedX += forceX;
	              shots[k].particles[i].speedY += forceY;
//	              particles[i].speedZ += forceZ;

	              // Take away some life
	              shots[k].particles[i].life -= shots[k].particles[i].fade;
	              if (shots[k].particles[i].life < 0.0f) {  // check for burst also
	            	  shots[k].particles[i].inactivate();
	              }
	           }
	        }
	        if (!ptclsActive){
	        	if(type==0){
	        	shots[k].setKasploded();
	        	}else if(type==1){
	        		target.setDestroyed();
	        		System.out.println("Target destroyed");
	        	}
	        }
	    }
	    
	    private void drawTerrain(GL2 gl){
	    	gl.glBegin(GL.GL_LINES);
	    	gl.glColor3f(0f, 0f,0f);
			for (int i=0;i<lines.length-1;i++){
				gl.glVertex2f(lines[i][0],lines[i][1]);
				gl.glVertex2f(lines[i+1][0], lines[i+1][1]);
			}
			gl.glEnd();
		}
	    
	    private void drawTurret(GL2 gl){
	    	   gl.glBegin(GL_LINE_LOOP);
	    	   float radius = gun.getRadius()/2;
	    	   float X = gun.getX();
	    	   float Y = gun.getY();
	    	   for (float i=0; i<Math.PI; i+=0.0175)
	    	   {
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
	    }
	    
	    public void checkCollision() {
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
//								shots[j].initPtcls(shots[j].getX(), shots[j].getY());
								shots[j].initPtcls(shots[j].getX(), y1+2);
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
								if (y1==y2){
									shots[j].particles[l].speedY=-shots[j].particles[l].speedY*0.999f;
									System.out.println("ptclColCheck1 yp="+yp);
								}else{
								double hsqd=Math.pow(x2-xC,2)+Math.pow(y2-yC,2);
								double msqd=Math.pow(x2-x1,2)+Math.pow(y2-y1,2);
								double dsqd=Math.pow(xC-x1,2)+Math.pow(yC-y1,2);
								double h=Math.sqrt(hsqd);
								double m=Math.sqrt(msqd);
								double theta=Math.acos((hsqd+msqd-dsqd)/(2*h*m));
								double k = h*Math.sin(theta);
								if(k<=shotRadius){
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
			}
	    
	    private static boolean intervallContains(float low, float high, float n) {
//			System.out.println("low="+low+"    high="+high+"      n="+n);
			if (n >= low && n <= high){
				return true;
			}else if(n >= high && n <= low){
				return true;
			}else{
				return false;
			}
//		    return n >= low && n <= high;
		}
	    
	    class Particle {
	        boolean active=true; // always active in this program
	        float life;     // life time
	        float fade;     // fading speed, which reduces the life time
	        float r, g, b;  // color
	        float x, y, z;  // position
	        float speedX, speedY, speedZ; // speed in the direction

	        private Random rand = new Random();

	        // Constructor
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
//	           speedZ = (rand.nextInt(50) - 25.0f) * 10.0f;
	           speedZ=0;
	           // Pick a random color from the colors array
//	           int colorIndex = rand.nextInt(colors.length);
//	           r = colors[colorIndex][0];
//	           g = colors[colorIndex][1];
//	           b = colors[colorIndex][2];
	           //YELLOW
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
	            
	            if ((key==com.jogamp.newt.event.KeyEvent.VK_SPACE)){
	            	if(SHOTCOUNT<MAXSHOTS){
	            	shots[SHOTCOUNT]=new BulletGL(gun.getX()+(int)((gun.getBarrelLength())*Math.cos(gun.getAngle()*Math.PI/180)),bulletStartY,gun.getPower(),gun.getAngle(),TICKCOUNT);
	            	SHOTCOUNT++;
	            	shotPresent=true;
	            	System.out.println("SHOT");
	            	}//TODO code here for trying to send power beyond 100
	            }
	        }

//			@Override
//			public void keyPressed(com.jogamp.newt.event.KeyEvent e) {
//			}

			@Override
			public void keyReleased(com.jogamp.newt.event.KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
	    }
}
