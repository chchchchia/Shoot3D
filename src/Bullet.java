import java.util.Random;




public class Bullet {
	private int x;
	private int xo;
	private int y;
	private int yo;
	private long tof;
	private double Vx;
	private double Vy;
	private double power;
	private double angle;
	private double time;
	private double INIticks;
	private double gravity=35;
	private double pwrFactor=1.5;
	private double boomTicks=0;
	private boolean Kasploded=false;
	private boolean booming=false;
	private int boomRad=1;
	private boolean gl;
	public int MAX_PARTICLES=250;
	public Particle[] particles = new Particle[MAX_PARTICLES];
	public boolean ptclsActive=false;
	//power factor is used to fine tune the power of the shot so it looks pretty on screen
	//tof = time the bullet has been in flight
	public Bullet(int x, int y, double power, double angle,double ticks){
		this.x=x;
		this.xo=x;
		this.y=y;
		this.yo=y;
		this.power=power*pwrFactor;
		this.angle=angle*Math.PI/180;
		this.time=0;
		this.INIticks=ticks;
	}

//The bullet right now is a rectangle, centered at x,y.
//Collision detection needs to account for this, searching x+-WIDTH
	/*
	 * public void move(double ticks){
		if (!this.Kasploded&&!this.booming){
		//called every so often
//		long currentTime=System.currentTimeMillis();
		this.time=(double)(ticks-INIticks)/16;
		//the denominator here controls the apparent speed of the ball
//		this.tof=(currentTime-this.time);
		//KINEMATICS!!!
		Vx=this.power*Math.cos(angle);
		double dx=Vx*time;
		this.x=xo+(int)dx;
		double Vyo=-power*2*Math.sin(angle);
		double Vyg=gravity*time;
		Vy=Vyo+Vyg;
		double dy = Vyo*time+(0.5*Vyg*time);
		this.y=(int)(yo)+(int)dy;
		
//		System.out.println("X="+this.x+"    Y="+this.y+"    dy="+dy);
//		System.out.println("Time="+time);
//		System.out.println(ticks);
		}else if (booming){
			if (boomRad>=40){
				this.setKasploded();
			}else {
			this.boomRad+=2;
			}
		}

		
	}
	 */
	public void move(double ticks){
		if (!this.Kasploded&&!this.booming){
		//called every so often
//		long currentTime=System.currentTimeMillis();
		this.time=(double)(ticks-INIticks)/16;
		//the denominator here controls the apparent speed of the ball
//		this.tof=(currentTime-this.time);
		//KINEMATICS!!!
		Vx=this.power*Math.cos(angle);
		double dx=Vx*time;
		this.x=xo+(int)dx;
		double Vyo=power*2*Math.sin(angle);
		double Vyg=-gravity*time;
		Vy=Vyo+Vyg;
		double dy = Vyo*time+(0.5*Vyg*time);
		this.y=(int)(yo)+(int)dy;
		
//		System.out.println("X="+this.x+"    Y="+this.y+"    dy="+dy);
//		System.out.println("Time="+time);
//		System.out.println(ticks);
		}else if (booming){
			if (boomRad>=40){
				this.setKasploded();
			}else {
			this.boomRad+=2;
			}
		}

		
	}
	public float getVx(){
		return (float)this.Vx;
	}
	public float getVy(){
		return (float)this.Vy;
	}
	public int getX(){
		return this.x;
	}
	
	public int getY(){
		return this.y;
	}
	public int getBoomRad(){
		return this.boomRad;
	}
	public void boom(){
		this.booming=true;
		this.boomTicks=time;
//		initPtcls(this.x, this.y);
	}
	public void setKasploded(){
		this.booming=false;
		this.Kasploded=true;
		this.x=0;
		this.y=0;
		this.ptclsActive=false;
	}
	public boolean isBooming(){
		return this.booming;
	}
	//TODO KASPLODE method, to destruct the bullet
	public boolean isBoomed(){
		return this.Kasploded;
	}
	public void initPtcls(float x, float y){
  	  // Initialize the particles
		this.ptclsActive=true;
	      for (int i = 0; i < MAX_PARTICLES; i++) {
	         particles[i] = new Particle(x,y);
//	         particles[i].setX(x);
//           particles[i].setY(y);
	      }
  }
}

class Particle {
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