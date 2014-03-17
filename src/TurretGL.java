
public class TurretGL {
//		private static boolean keyPress = false;
		private int x;
		private int y;
		private double angle;
		private double power;
		private int radius=18;
		private int barrelLength=18;
		private double bX1;
		private double bY1;
		private double bX2;
		private double bY2;
		//these are fudge factors for alignment of the barrel to the arc of the turret
		private int arcX=this.x-this.radius/2+5;
		private int arcY=this.y-this.radius/2+1;
		
		TurretGL(int x, int y){
			this.angle=45;
			this.x=x;
			this.y=y;
			this.power=35;
			bX1= ((arcX+this.radius/2)+(this.radius/2)*Math.cos(this.angle*Math.PI/180));
			bY1= ((arcY+this.radius/2)-(this.radius/2)*Math.sin(this.angle*Math.PI/180));
			bX2= ((arcX+this.radius/2)+(this.barrelLength)*Math.cos(this.angle*Math.PI/180));
			bY2= ((arcY+this.radius/2)-(this.barrelLength)*Math.sin(this.angle*Math.PI/180));
		}
		
	//TODO SLEW method for 3d mode
		//The angles here are in degrees! They are converted to radians in the bullet class
		public void aim(double dTheta){
			this.angle+=dTheta;
			if (this.angle<0){
				this.angle=0;
			}
			if (this.angle>180){
				this.angle=180;
			}
			bX1=(arcX+this.radius/2)+(this.radius/2)*Math.cos(this.angle*Math.PI/180);
			bY1= ((arcY+this.radius/2)-(this.radius/2)*Math.sin(this.angle*Math.PI/180));
			bX2= ((arcX+this.radius/2)+(this.barrelLength)*Math.cos(this.angle*Math.PI/180));
			bY2= (arcY+this.radius/2)-(this.barrelLength)*Math.sin(this.angle*Math.PI/180);
		}
		
		public void power(double dPower){
			this.power+=dPower;
			if (this.power<0){
				this.power=0;
			}
			if (this.power>100){
				this.power=100;
			}
		}

	public double getbX1(){
		return this.bX1;
	}
	public double getbY1(){
		return this.bY1;
	}
	public double getbX2(){
		return this.bX2;
	}
	public double getbY2(){
		return this.bY2;
	}
	public int getX(){
			return this.x;
		}
		
	public int getY(){
			return this.y;
		}

	public int getRadius(){
		return this.radius;
	}
	public double getAngle(){
		return this.angle;
	}
	public double getPower(){
		return this.power;
	}
	public int getBarrelLength(){
		return this.barrelLength;
	}
}
