import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;


public  class Ballz extends Ellipse2D.Double {
	int dx;
	int dy;
	
	public Ballz(double x, double y, double w, double h, int dx, int dy) {
        setFrame(x, y, w, h);
        this.dx=dx;
        this.dy=dy;
    }
	
//TODO override boolean equals method to include dx and dy
	
	public Ballz(double x, double y, double w, double h) {
        setFrame(x, y, w, h);
        Random rnd=new Random(42);
        this.dx=(int)((Math.signum(Math.random()))*rnd.nextInt(10));
        this.dy=(int)((Math.signum(Math.random()))*rnd.nextInt(10));
    }

	public int getDx(){
		return this.dx;
	}
	
	public int getDy(){
		return this.dy;
	}
	
	public void setFrame(double x, double y, double w, double h, int dx, int dy) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
        this.dx=dx;
        this.dy=dy;
    }
	
}
