package ShootingGL;
import java.awt.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.*;
import org.apache.commons.lang.ArrayUtils;
public class Terrain {	
	//the points along the lines defining the terrain
	//0deg
	private int lines0[][]={{0,40},{90,40},{160,200},{190,150},{260,310},{320,190},{340,220},{420,90},{750,90}};
	//60deg
	private int lines60[][]={{0,40},{80,40},{100,40},{120,20},{190,180},{220,210},{240,250},{260,210},{340,140},{460,160},{600,120},{680,120},{750,90}};
	//180 deg
	private int lines180[][]={{0,40},{50,40},{70,30},{117,48},{170,100},{255,24},{353,20},{375,50},{408,28},{439,123},{475,45},{620,45},{670,65},{725,100},{750,80}};
	//250deg
	private int lines250[][]={{0,26},{25,40},{50,40},{75,35},{110,50},{165,45},{185,65},{215,55},{230,50},{250,65},{260,65},{270,80},{300,80},{320,65},{380,70},{405,85},{440,85},{460,165},{480,80},{505,80},{525,140},{540,70},{620,45},{680,70},{720,70},{750,50}};
	//300deg
	private int lines300[][]={{0,40},{90,40},{110,60},{130,40},{170,40},{230,250},{250,230},{280,320},{320,240},{340,240},{350,230},{360,250},{450,10},{490,50},{530,50},{590,30},{630,80},{650,60},{750,60}};
	//[deg][Point]
	private Point [][] linePoints=new Point[360][];
	//[heading][coeff][line]
	private int definedHeadings[]={0,60,180,250,300};
	//lineEqnCoeffs is [deg][coeff][line]
	private double[][][] lineEqnCoeffs=new double[360][3][];
	public boolean TransitionsCreated=false;
	public Terrain(){
		//TODO in the future, this could load up a data file
		
	}

	public void loadPoints(){
		//future version should read these points from a file, load them into the array
		linePoints[0]=new Point[lines0.length];
		for (int i=0;i<lines0.length;i++){
			linePoints[0][i]=new Point(lines0[i][0],lines0[i][1]);
		}
		linePoints[60]=new Point[lines60.length];
		for (int i=0;i<lines60.length;i++){
			linePoints[60][i]=new Point(lines60[i][0],lines60[i][1]);
		}
		linePoints[180]=new Point[lines180.length];
		for (int i=0;i<lines180.length;i++){
			linePoints[180][i]=new Point(lines180[i][0],lines180[i][1]);
		}
		linePoints[250]=new Point[lines250.length];
		for (int i=0;i<lines250.length;i++){
			linePoints[250][i]=new Point(lines250[i][0],lines250[i][1]);
		}
		linePoints[300]=new Point[lines300.length];
		for (int i=0;i<lines300.length;i++){
			linePoints[300][i]=new Point(lines300[i][0],lines300[i][1]);
		}	
		//Now init the deg's not yet created
		for (int i=0;i<definedHeadings.length;i++){
			if(i<definedHeadings.length-1){
				for(int deg=definedHeadings[i]+1;deg<definedHeadings[i+1];deg++){
					linePoints[deg]=new Point[linePoints[definedHeadings[i]].length+linePoints[definedHeadings[i+1]].length-1];			
				}
			}else{
				for(int deg=definedHeadings[i]+1;deg<360;deg++){
					linePoints[deg]=new Point[linePoints[definedHeadings[i]].length+linePoints[0].length-1];
				}
			}
		}
		
	}
	
	public double[][][] getLineCoeffs(){
		//[deg][coeff][line]
		if(TransitionsCreated){
		return lineEqnCoeffs;
		}else throw new RuntimeException("Please call CreateTransitions() before trying to get line coefficients. kthxbye");
	}
	
	public void CreateTransitions(){
		//this method creates transitions between the prescribed levels (see lines defined above)
		//If there's a prescribed level for each deg, then this is not needed
		//This should be run in the init of the terrain
		Point[] PrevSet=linePoints[definedHeadings[definedHeadings.length-1]];
		System.out.println(definedHeadings[definedHeadings.length-1]);
		int prevHeading=definedHeadings[definedHeadings.length-1];
		createLinesCoeffs();
		//first lets work forward, starting with 0deg->next defined deg
		//Then do the reverse
		for(int i:definedHeadings){
			for(int k=0;k<PrevSet.length;k++){
				Point p = PrevSet[k];
				double calcY=pointFactory(p.getX(),prevHeading,i);
				double diffY=calcY-p.getY();
				if(i==definedHeadings[0]){
					double yStep=diffY/(360+i-prevHeading);
					for (int d=prevHeading+1;d<360+definedHeadings[0];d++){
						if(d>=360){
							d=d-360;
						}
						linePoints[d][k]=new Point(p.getX(),p.getY()+yStep*(d-prevHeading));
					}
				}else{
					double yStep=diffY/(i-prevHeading);
					for (int d=prevHeading+1;d<i;d++){
						linePoints[d][k]=new Point(p.getX(),p.getY()+yStep*(d-prevHeading));
					}
				}
			}
			for(int m=0;m<linePoints[i].length;m++){
				Point p = linePoints[i][m];
				System.out.println("deg="+i+" x="+p.getX()+" PF="+pointFactory(p.getX(),i,prevHeading));
				double calcY=pointFactory(p.getX(),i,prevHeading);
				double diffY=p.getY()-calcY;
				if(i==definedHeadings[0]){
					double yStep=diffY/(360+i-prevHeading);
					for (int d=prevHeading+1;d<360+definedHeadings[0];d++){
						if(d>=360){
							d=d-360;
						}
						linePoints[d][PrevSet.length-1+m]=new Point(p.getX(),calcY+yStep*(d-prevHeading));
					}
				}else{
					double yStep=diffY/(i-prevHeading);
					for (int d=prevHeading+1;d<i;d++){
						linePoints[d][PrevSet.length-1+m]=new Point(p.getX(),calcY+yStep*(d-prevHeading));
					}
				}
			}
			PrevSet=linePoints[i];
			prevHeading=i;
			System.out.println(linePoints[prevHeading+1]);
		}
		//Now sort the points
		for(int deg=0;deg<360;deg++){
			Arrays.sort(linePoints[deg], new PointXComparator());
			//Along the way, remove duplicate points
			for(int i=0;i<linePoints[deg].length-1;i++){
				if(linePoints[deg][i].getX()==linePoints[deg][i+1].getX()){
					linePoints[deg]=(Point[]) ArrayUtils.remove(linePoints[deg], i+1);
				}
			}
		}
		createLinesCoeffs();
		TransitionsCreated=true;
	}
	
	private void createLinesCoeffs(){
		//this calcs the coeffs needed for the slope and y intercept of the line
		double coeff1;
		double coeff2;
		double coeff3;
//		boolean doAllPoints=false;
//		if(linePoints[definedHeadings[0]+1]!=null)
//		for (int i:definedHeadings){
		for(int i=0;i<360;i++){
			if(linePoints[i][0]==null){
				continue;
			}
			lineEqnCoeffs[i][0]=new double[linePoints[i].length];
			lineEqnCoeffs[i][1]=new double[linePoints[i].length];
			lineEqnCoeffs[i][2]=new double[linePoints[i].length];
			for(int j=0;j<linePoints[i].length-1;j++){
				if(linePoints[i][j].getX()!=linePoints[i][j+1].getX()){
					coeff1=(linePoints[i][j+1].getY()-linePoints[i][j].getY())/(linePoints[i][j+1].getX()-linePoints[i][j].getX());
					coeff2=linePoints[i][j].getX()*coeff1;
					coeff3=linePoints[i][j].getY();
				}else{
					//if x1=x2, then it's a vertical line
					coeff1=0;
					coeff2=0;
					coeff3=linePoints[i][j].getY();
				}
				lineEqnCoeffs[i][0][j]=coeff1;
				lineEqnCoeffs[i][1][j]=coeff2;
				lineEqnCoeffs[i][2][j]=coeff3;
			}
		}
	}
	
	private double pointFactory(double x, int fromHeading, int toHeading){
		//this method maps an x value from fromHEading to a line in toHeading and returns the corresponding y value from the point on that line
		int p=-1;
		if(fromHeading==definedHeadings[definedHeadings.length-1]){
			for(int i=0;i<linePoints[toHeading].length;i++){
				if(x<linePoints[toHeading][i].getX()){
					p=i-1;
					break;
				}else if(x==750){
					p=linePoints[toHeading].length-2;
					break;
				}
			}
				if(p==-1){
					throw new IllegalArgumentException("x is out of range! x="+x);
				}
		}else{
			for(int i=0;i<linePoints[toHeading].length;i++){
				if(x<linePoints[toHeading][i].getX()){
					p=i-1;
					break;
				}else if(x==750){
					p=linePoints[toHeading].length-2;
					break;
				}
			}
				if(p==-1){
					throw new IllegalArgumentException("x is out of range!");
				}
		}
		double slope=lineEqnCoeffs[toHeading][0][p];
		double coeff2=lineEqnCoeffs[toHeading][1][p];
		double coeff3=lineEqnCoeffs[toHeading][2][p];
		System.out.println("For x="+x+" in deg="+fromHeading+", y="+slope+"x-"+coeff2+"+"+coeff3+" in deg="+toHeading+"with p="+p);
		return slope*x-coeff2+coeff3;				
	}
	
	public Point[] getLines(int deg){
		if(deg>=0&&deg<=360){
			return linePoints[deg];
		}else {
			throw new IllegalArgumentException("deg is out of range!");
		}
	}

}
class PointXComparator implements Comparator<Point>{
	@Override
	public int compare(Point a, Point b){
		if (a.getX()>b.getX()){
			return 1;
		}else if(a.getX()<b.getX()){
			return -1;
		}else{
			return 0;
		}
//		return (a.getX()>b.getX() ? 1:-1);
	}
}
