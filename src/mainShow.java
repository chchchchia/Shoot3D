import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;


public class mainShow extends JFrame {
	private static final long serialVersionUID = 1L;
	screen screen;
	Graphics g;
//	BufferStrategy bs;
	public mainShow() {

        initUI();
    }

    private void initUI() {
//    	this.createBufferStrategy(2);
    	screen=new screen();
    	add(screen);

        setTitle("Shoot");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(Color.white);
        setSize(750, 520);
        setLocationRelativeTo(null);
       
        
    }
    
 /*   public void paint(Graphics g){
		if(bs == null){
			createBufferStrategy(3);
			bs = getBufferStrategy();
		}
		try {
			g = bs.getDrawGraphics();
			Renderer.renderGame(world, (Graphics2D)g);
		} finally {
			g.dispose();
		}
		bs.show();
		Toolkit.getDefaultToolkit().sync();
	}
*/
    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                mainShow mS = new mainShow();
                mS.setVisible(true);
            }
        });
    }
}
