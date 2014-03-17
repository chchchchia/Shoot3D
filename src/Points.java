import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;


import javax.swing.Timer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

class Surface extends JPanel implements ActionListener{

	public Timer timer;
	private int x=0;
	private int y=0;
	public int TICK =30;
	public int TICKCOUNT=0;
	public Bullet bullet;
	int ballzCount=51;
	int wallzCount=1;
	Ballz[] ballz = new Ballz[ballzCount];
	Rectangle2D[] barrier=new Rectangle2D.Double[wallzCount];
	
	
	 public Surface() {
	        initSurface();
	        initBarrier();
	        initBallz();
	        initTimer();
	        initBullet();
	    }
	 
	 private void initBullet(){
		 bullet=new Bullet(75,this.getHeight()-75,100,15,TICKCOUNT);
		 
	 }
	 
	 private void initBallz() {
//TODO fix random numbers being negative
		 Random rnd = new Random(42);
	    	for (int i=0;i<=ballzCount-1;i++){
//	    		ballz[i]=new Ballz(this.x+rnd.nextInt(this.getWidth()), y+rnd.nextInt(this.getHeight()), 10, 10,(int)((Math.signum(Math.random()))*rnd.nextInt(10)),(int)((Math.signum(Math.random()))*rnd.nextInt(10)));
//this version starts everyone on the left side	    		
	    		ballz[i]=new Ballz(this.x+rnd.nextInt(this.getWidth()/2), y+rnd.nextInt(this.getHeight()), 10, 10,(int)((Math.signum(Math.random()))*rnd.nextInt(10)),(int)((Math.signum(Math.random()))*rnd.nextInt(10)));
	    		if (barrier[0].contains(ballz[i].getX(), ballz[i].getY(), ballz[i].getWidth(),	ballz[i].getHeight())||barrier[0].intersects(ballz[i].getX(), ballz[i].getY(), ballz[i].getWidth(),	ballz[i].getHeight())){
	    			i=i-1;
	    		}
	    	}
	}
	 private void initBarrier(){
		 //start with just one
		 barrier[0]=new Rectangle2D.Double(this.getWidth()/2+1,0,45,this.getHeight()-(this.getHeight()/5));
	 }
	private void initSurface() {
		setSize(750, 500);  
		 setBackground(Color.white);
//	        ellipses = new Ellipse2D.Float[25];
//	        esize = new double[ellipses.length];
//	        estroke = new float[ellipses.length];           
	    } 
	 
	 private void initTimer() {
	        
	        timer = new Timer(TICK, this);
	        timer.setInitialDelay(190);
	        timer.start();        
	    }
	 
	 
	    public void actionPerformed(ActionEvent e) {

	        TICKCOUNT+=1;
	        moveBullet();
	    	moveBallz();
	        repaint();
	    }
	private void doDrawing(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.blue);

        Dimension size = getSize();
        Insets insets = getInsets();
        RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHints(rh);
        int w = size.width - insets.left - insets.right;
        int h = size.height - insets.top - insets.bottom;
        drawBarriers(g2d);
        drawLines(g2d);
        drawBullets(g2d);
        g2d.drawArc(0, 0, 35, 35, 0, 180);
    }

	private void moveBullet(){
		//for loop here, eventually, for an array of bullets
		bullet.move(TICKCOUNT);
	}
	private void moveBallz(){

		for (int i=0;i<=ballz.length-1;i++){
			int dx=ballz[i].getDx();
			int dy=ballz[i].getDy();
			double locheight=ballz[i].getHeight();
			double locwidth=ballz[i].getWidth();
			double bx= ballz[i].getX();
			double by=ballz[i].getY();
			
			if (barrier[0].intersects(bx, by, locwidth,	locheight)){  
				//more glorified code here to check if it's an x or y collision
				dx=-1*dx;
				dy=-1*dy;
			}else{
				//Since no collision with box, check to see if it hit a wall
				if (ballz[i].getX()+dx>=this.getWidth()||ballz[i].getX()+dx<=0){
					dx=-1*dx;
				}
				if (ballz[i].getY()+dy>=this.getHeight()||ballz[i].getY()+dy<=0){
					dy=-1*dy;
				}
			}
			ballz[i].setFrame(ballz[i].getX()+dx, ballz[i].getY()+dy, ballz[i].getWidth(), ballz[i].getHeight(),dx,dy);
		}
		
	}
	private void drawBullets(Graphics2D g2d){
//		g2d.drawOval(bullet.getX()+20, bullet.getY()-300, 10, 10);
	}
	private void drawBarriers(Graphics2D g2d){
/*		for (int i=0;i<wallzCount;i++){
			g2d.drawRect((int)barrier[i].getX(), (int)barrier[i].getY(),(int) barrier[i].getWidth(), (int)barrier[i].getHeight());
		}
		*/
//		g2d.drawRect((int)barrier[0].getX(), (int)barrier[0].getY(),(int) barrier[0].getWidth(), (int)barrier[0].getHeight());
	}
    private void drawLines(Graphics2D g2d){
    	for (int i=0;i<=ballzCount-1;i++){
    		g2d.drawOval((int)ballz[i].getX(),(int)ballz[i].getY(),(int)ballz[i].getWidth(),(int)ballz[i].getHeight());
    	}
//    	g2d.drawOval(this.x, this.y, 10, 10);
/*    	g2d.drawLine(30, 30, 200, 30);
        g2d.drawLine(200, 30, 30, 200);
        g2d.drawLine(30, 200, 200, 200);
        g2d.drawLine(200, 200, 30, 30);
        g2d.drawRect(0, 0, 50, 75); */
    	g2d.drawRect((int)barrier[0].getX(), (int)barrier[0].getY(),(int) barrier[0].getWidth(), (int)barrier[0].getHeight());
    	g2d.fillOval(bullet.getX(), bullet.getY(), 10, 10);
    }
    
    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        doDrawing(g);
    }
}

public class Points extends JFrame {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Points() {

        initUI();
    }

    private void initUI() {
    	add(new Surface());
        setTitle("Ghost Ballz");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(Color.white);
        setSize(750, 500);
        setLocationRelativeTo(null);
    }
    
 

    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                Points ps = new Points();
                ps.setVisible(true);
            }
        });
    }
}