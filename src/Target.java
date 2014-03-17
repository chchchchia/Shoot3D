import static javax.media.opengl.GL.GL_TRIANGLE_STRIP;

import java.util.Random;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;




public class Target {
private final int HEIGHT;
private final int WIDTH;
private int x;
private int y;
private boolean isBooming=false;
private int boomRad=1;
private double boomTicks=0;
private boolean Destroyed;
private static float forceX = 0.0f;
private static float forceY = -10.0f;
private static float slowdown = 2.0f;

private float textureTop, textureBottom, textureLeft, textureRight;
private int MAX_PARTICLES=500;
public TParticle[] particles = new TParticle[MAX_PARTICLES];
//TODO Create type system for different targets, shapes, explosion type (fire and smoke vs nuclear)

public Target(int x, int y, int w, int h){
	this.x=x;
	this.y=y;
	this.WIDTH=w;
	this.HEIGHT=h;
	this.Destroyed=false;
}
public void setDestroyed(){
	this.Destroyed=true;
	this.isBooming=false;
}
public boolean isDestroyed(){
	return Destroyed;
}
public int getBoomRad(){
	this.boomRad=this.boomRad+2;
	if(boomRad>70){
		this.Destroyed=true;
		this.isBooming=false;
		return this.boomRad;
	}else{
	return this.boomRad;
	}
}
public int getWidth(){
	return this.WIDTH;
}
public int getHeight(){
	return this.HEIGHT;
}
public int getX(){
	return this.x;
}
public int getY(){
	return this.y;

}
public boolean isBooming(){
	return this.isBooming;
}
public void setHit(){
	this.isBooming=true;
}
public void initPtcls(float x, float y){
	  // Initialize the particles

    for (int i = 0; i < MAX_PARTICLES; i++) {
       particles[i] = new TParticle(x,y);
//       particles[i].setX(x);
//       particles[i].setY(y);
    }

}
public void drawBoom(GL2 gl, Texture texture){
	// Render the particles
	TextureCoords textureCoords = texture.getImageTexCoords();
    textureTop = textureCoords.top();
    textureBottom = textureCoords.bottom();
    textureLeft = textureCoords.left();
    textureRight = textureCoords.right();
	boolean ptclsActive=false;
    for (int i = 0; i < MAX_PARTICLES; i++) {
//    	System.out.println("Active="+particles[i].active);
       if (particles[i].active) {
    	   ptclsActive=true;
          // Draw the particle using our RGB values, fade the particle based on it's life
          gl.glColor4f(particles[i].r, particles[i].g, particles[i].b, particles[i].life);
//          System.out.println("Draw Boom "+i+"Y="+particles[i].y);
          texture.enable(gl);
          texture.bind(gl);

          gl.glBegin(GL_TRIANGLE_STRIP); // build quad from a triangle strip
          float px = particles[i].x;
          float py = particles[i].y;
          float pz = particles[i].z;
//use a vertex3f for a more 3d effect...maybe
          
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
          particles[i].x += particles[i].speedX / (slowdown * 1000.0f);
          particles[i].y += particles[i].speedY / (slowdown * 1000.0f);
//          shots[k].particles[i].z += shots[k].particles[i].speedZ / (slowdown * 1000.0f);

          // Apply the gravity force
          particles[i].speedX += forceX;
          particles[i].speedY += forceY;
//          particles[i].speedZ += forceZ;

          // Take away some life
          particles[i].life -= particles[i].fade;
//          System.out.println("Life= "+particles[i].life);
          if (particles[i].life < 0.0f) {  // check for burst also
             // If the particle is dead (burnt out), we'll rejuvenate it. We do
             // this by giving it full life and a new fade speed.
//             particles[i].regenerate();
//        	  System.out.println("INACTIVATE");
        	  particles[i].inactivate();
//        	  System.out.println("fizzle");
          }
          
//          if (enabledBurst) {
//             particles[i].burst();
//          }
       }
    }
    if (!ptclsActive){ 	
    		this.setDestroyed();
    		System.out.println("Target destroyed"); 	
    }
}

}

class TParticle {
    boolean active=true; // always active in this program
    float life;     // life time
    float fade;     // fading speed, which reduces the life time
    float r, g, b;  // color
    float x, y, z;  // position
    float speedX, speedY, speedZ; // speed in the direction
    float speedXGlobal=10;
    float speedYGlobal=10;
    private Random rand = new Random();

    // Constructor
    public TParticle(float x, float y) {
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
//       life = 1.0f;

       // Set a random fade speed value between 0.003 and 0.103
       fade = rand.nextInt(100) / 1000.0f + 0.03f;

       // Set the initial position
//       x = y = z = 0.0f;
       
       // Generate a random speed and direction in polar coordinate, then resolve
       // them into x and y.
       // Set Random speed between -25 to +25
       float speed = (rand.nextInt(50) - 25.0f);
       float angle = (float)Math.toRadians(rand.nextInt(360));

       // Multiplied by 10 to create a spectacular explosion when the program first starts
       speedX = speed * (float)Math.cos(angle) * 100.0f;
       speedY = speed * (float)Math.sin(angle) * 100.0f;
//       speedZ = (rand.nextInt(50) - 25.0f) * 10.0f;
       speedZ=0;
       // Pick a random color from the colors array
//       int colorIndex = rand.nextInt(colors.length);
//       r = colors[colorIndex][0];
//       g = colors[colorIndex][1];
//       b = colors[colorIndex][2];
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
