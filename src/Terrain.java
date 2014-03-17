import java.awt.Graphics;


public class Terrain {

//This defines the terrain for the level.
/*
//Should have a way to differentiate between levels - ie, a change level method or a create terrain method that takes, as a param, which level
	public Terrain(int ox,int oy, int dx,int dy){
		this.ox=ox;
		this.oy=oy;
		this.dx=dx;
		this.dy=dy;
	}
	*/
	
	
	//the points along the lines defining the terrain
//	private int lines[][]={{0,480},{90,480},{160,320},{190,370},{260,210},{320,330},{340,300},{420,430},{750,430}};
	
	//0deg
	private int lines0[][]={{0,40},{90,40},{160,200},{190,150},{260,310},{320,190},{340,220},{420,90},{750,90}};

	//60deg
	private int lines60[][]={{0,40},{80,50},{100,40},{120,20},{190,180},{220,210},{240,250},{260,210},{340,140},{460,160},{600,120},{680,120},{750,90}};
	//300deg
	private int lines300[][]={{0,40},{90,40},{110,60},{130,40},{170,40},{230,250},{250,230},{280,320},{320,240},{340,240},{350,230},{360,250},{450,10},{490,50},{530,50},{590,30},{630,80},{650,60},{750,60}};
	//Please, only call this once
	public Terrain(){
		//in the future, this could load up a data file
	}
	public void draw(Graphics g){
		
	}
	public int[][] getLines(){
		return lines0;
	}
	
	//to be totally rigerous, I could add get X and get Y methods here for each entry in the array, BUT
	//I'm probably not going to keep this setup in the end, so screw it
}
