import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.Timer;


public class screen extends JPanel implements ActionListener {

	private Timer timer;
	public int TICK =30;
	public int TICKCOUNT=0;
	private int MAXSHOTS=100;
	private int SHOTCOUNT=0;
	private boolean shotPresent=false;
	private final int WIDTH = 750;
	private final int HEIGHT = 520;
	private final int DELAY = 150;
	private boolean left = false;
	private boolean right = true;
	private boolean up = false;
	private boolean down = false;
	private int bulletStartY;
	private Bullet[] shots= new Bullet[MAXSHOTS];
	private Turret gun;
	private Graphics2D g2d;
	private Terrain terr=new Terrain();
	private Target target;
	int[][] lines;
	private int score=0;
	//test polygon
	private Polygon test=new Polygon();
	BufferStrategy bf;
	
	//as you might of guessed, this is where the jpanel lives. Calls are made to this class to draw the screen.
	public screen() {
		addKeyListener(new TAdapter());
		setFocusable(true);
        initGame();
        initTerrain();
        initTimer();
        initTurret();
        initTarget();
        this.setDoubleBuffered(true);

	}
	
	private void initTarget(){
		this.target=new Target(600,405,25,25);
	}
	private void initTerrain(){
		this.lines=terr.getLines();
		
	}
	private void initTurret(){
		gun=new Turret(35,460);
	}
	
	private void initShots(){
		 shots[0]=new Bullet(75,this.getHeight()-75,100,15,TICKCOUNT);
		 
	 }
	 
	public void initGame(){
		//This should be where the terrain is drawn, initial objects created
//		Terrain terrain = new Terrain(something something something)
//to start off, let's create a rectangle
	}
	
	private void initTimer() {
        
        timer = new Timer(TICK, this);
        timer.setInitialDelay(190);
        timer.start();        
    }
 
 
    public void actionPerformed(ActionEvent e) {

        TICKCOUNT+=1;
        keypress();
        moveShots();
        checkCollision();
 //       drawShots(g2d);
        repaint();
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
    
    private void moveShots(){
		//for loop here, eventually, for an array of bullets
    	if (shotPresent){
    		for(int i=0;i<SHOTCOUNT;i++)
		shots[i].move(TICKCOUNT);
    	}
	}
    
	private void doDrawing(Graphics g) {
	
       this.g2d = (Graphics2D) g;
      
        g2d.setColor(Color.blue);
        
        Dimension size = getSize();
        Insets insets = getInsets();
        RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHints(rh);
        int w = size.width - insets.left - insets.right;
        int h = size.height - insets.top - insets.bottom;
        drawTerrain(g2d);
        drawShots(g2d);
        drawTurret(g2d);
        drawTarget(g2d);
//		g2d.drawArc(18,470,18,18,0,180);

        g2d.drawString("Power="+this.gun.getPower(), 0, 12);
        g2d.drawString("Angle="+this.gun.getAngle(), 0, 24);
        g2d.drawString("Score="+this.score, 0, 36);
      g2d.dispose();
       Toolkit.getDefaultToolkit().sync();
	}
	private void drawTarget(Graphics2D g2d){
		if (target.isBooming()){
			int boomRad=target.getBoomRad();
			g2d.drawOval(target.getX()+target.getWidth()/2-boomRad/2,target.getY()+target.getHeight()/2-boomRad, boomRad, boomRad);
		}else if(!target.isDestroyed()){
		g2d.drawRect(target.getX(), target.getY(), target.getWidth(), target.getHeight());
		}
	}
	private void drawTerrain(Graphics2D g2d){
		for (int i=0;i<lines.length-1;i++){
			g2d.drawLine(lines[i][0], lines[i][1], lines[i+1][0], lines[i+1][1]);
		}
	}
	private void drawShots(Graphics2D g2d) {
		if (shotPresent){
			for(int i=0;i<SHOTCOUNT;i++){
				if (!shots[i].isBoomed()){
					if (shots[i].isBooming()){
						//NOTE: not adjusting the x and y by boomRad/2 makes the circle expand downward, like it's raining boom
						int boomRad=shots[i].getBoomRad();
						g2d.drawOval(shots[i].getX()-boomRad/2, shots[i].getY()-boomRad/2, boomRad, boomRad);
					}else{
				g2d.fillOval(shots[i].getX(), shots[i].getY(), 10, 10);
					}}
				
			}

//		System.out.println("SHOTS DRAWN");
		}
	}
	
	private void drawTurret(Graphics2D g2d){
//		Arc2D.Double tutt=new Arc2D.Double(gun.getX()-gun.getRadius(), gun.getY()+gun.getRadius(), gun.getRadius(), gun.getRadius(), 0, 180, Arc2D.OPEN);
//		g2d.draw(tutt);y2
//		int gunRad=gun.getRadius();
		int arcX=gun.getX()-gun.getRadius()/2+5;
		int arcY=gun.getY()+gun.getRadius()/2+1;
		
		g2d.drawArc(arcX, arcY, gun.getRadius(), gun.getRadius(), 0, 180);

		//Significant fudging to get this to look correct
		double x1=(arcX+gun.getRadius()/2)+(gun.getRadius()/2)*Math.cos(gun.getAngle()*Math.PI/180);
		double y1=(arcY+gun.getRadius()/2)-(gun.getRadius()/2)*Math.sin(gun.getAngle()*Math.PI/180);
		double x2=(arcX+gun.getRadius()/2)+(gun.getBarrelLength())*Math.cos(gun.getAngle()*Math.PI/180);
		double y2=(arcY+gun.getRadius()/2)-(gun.getBarrelLength())*Math.sin(gun.getAngle()*Math.PI/180);
		bulletStartY=(int)y2;
//		System.out.println("bX1="+gun.getbX1()+"   bX2="+gun.getbX2()+"   bY2="+gun.getbY2());
//		g2d.drawLine((int)gun.getbX1(),(int)gun.getbY1(),(int)gun.getbX2(),(int)gun.getbY2());
		g2d.drawLine((int)x1,(int) y1,(int) x2,(int) y2);
	}
	
	@Override
    public void paintComponent(Graphics g){    
		BufferedImage bufferedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g2d = bufferedImage.createGraphics();
	    //paint using g2d ...
	    doDrawing(g2d);
	    Graphics2D g2dComponent = (Graphics2D) g;
	    g2dComponent.drawImage(bufferedImage, null, 0, 0);
		
		
 //       super.paintComponent(g);
//        doDrawing(g);
    }

	
	public void checkCollision() {
		//code for collision with terrain, target
		//needs to destruct shots, reduce shotcount if they his something
		//TODO code for blowing up the user's gun
		int shotRadius=5;
		int buffer=0;
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
				int ym=shots[j].getY()+2*shotRadius;
				int yC=ym-shotRadius;
				int x1=lines[i][0];
				int x2=lines[i+1][0];
				int y1=lines[i][1];
				int y2=lines[i+1][1];
				if (intervallContains(x1,x2,xp+buffer)||intervallContains(x1,x2,xm-buffer)){
					if(intervallContains(y1,y2,yp-buffer)||intervallContains(y1,y2,ym+buffer)||y1==y2){
						double hsqd=Math.pow(x2-xC,2)+Math.pow(y2-yC,2);
						double lsqd=Math.pow(x2-x1,2)+Math.pow(y2-y1,2);
						double dsqd=Math.pow(xC-x1,2)+Math.pow(yC-y1,2);
						double h=Math.sqrt(hsqd);
						double l=Math.sqrt(lsqd);
						double theta=Math.acos((hsqd+lsqd-dsqd)/(2*h*l));
						double k = h*Math.sin(theta);
						if(k<=shotRadius){
							shots[j].boom();
							}
						}
					}
				//OK Now that we've checked the terrain, check to see if we've hit the target
				int xT=target.getX();
				int yT=target.getY();
				if(intervallContains(xT,xT+target.getWidth(),xp)||intervallContains(xT,xT+target.getWidth(),xm)){
					if(intervallContains(yT,yT+target.getHeight(),yp)||intervallContains(yT,yT+target.getHeight(),ym)){
						shots[j].boom();
						target.setHit();
						this.score++;
					}
				}
				}
				
				
			}
			}
		}
			
				
			
//	    	}
	
	private static boolean intervallContains(int low, int high, int n) {
//		System.out.println("low="+low+"    high="+high+"      n="+n);
		if (n >= low && n <= high){
			return true;
		}else if(n >= high && n <= low){
			return true;
		}else{
			return false;
		}
//	    return n >= low && n <= high;
	}
	 private class TAdapter extends KeyAdapter {

	        public void keyPressed(KeyEvent e) {

	            int key = e.getKeyCode();

	            if ((key == KeyEvent.VK_LEFT) && (!right)) {
	                left = true;
	                up = false;
	                down = false;
	            }

	            if ((key == KeyEvent.VK_RIGHT) && (!left)) {
	                right = true;
	                up = false;
	                down = false;
	            }

	            if ((key == KeyEvent.VK_UP) && (!down)) {
	                up = true;
	                right = false;
	                left = false;
	            }

	            if ((key == KeyEvent.VK_DOWN) && (!up)) {
	                down = true;
	                right = false;
	                left = false;
	            }
	            
	            if ((key==KeyEvent.VK_SPACE)){
	            	if(SHOTCOUNT<MAXSHOTS){
	            	shots[SHOTCOUNT]=new Bullet(gun.getX(),bulletStartY,gun.getPower(),gun.getAngle(),TICKCOUNT);
	            	SHOTCOUNT++;
	            	shotPresent=true;
//	            	System.out.println("SHOT");
	            	}//TODO code here for trying to send power beyond 100
	            }
	        }
}}
