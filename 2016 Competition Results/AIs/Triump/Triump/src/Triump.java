import structs.FrameData;
import structs.GameData;
import structs.Key;
import gameInterface.AIInterface;
import commandcenter.CommandCenter;
import enumerate.State;
public class Triump implements AIInterface {
	Key inputKey;
	FrameData  fd;
	CommandCenter cc;
	boolean playerNumber;
	boolean p;
	GameData gd;
	String Mystate;
	String Enemystate;
	int Myhit;
	int Enemyhit;
	int Myhp;
	int Enemyhp;
	boolean hit;
	boolean myhit;
	int temp;
	
	boolean use = true;
	int AreaXL=0, AreaXR=0, AreaYT=0, AreaYB=0;
	int MyX=0, MyY=0, OppX=0, OppY=0, EnemyL=0,EnemyR=0;	
	
	
	
	public void close() {
		// TODO Auto-generated method stub
	}
	
	public String getCharacter() {
		
		// TODO Auto-generated method stub
		return CHARACTER_ZEN;
	}
	
	public void getInformation(FrameData frameData) {
		// TODO Auto-generated method stub
		fd = frameData;
		cc.setFrameData(frameData, p);
	}
	
	public int initialize(GameData gameData, boolean playerNumber) {
		// TODO Auto-generated method stub
		gd = gameData;
		p = playerNumber;
		inputKey = new Key();
		fd = new FrameData();
		cc = new CommandCenter();	
		return 0;
	}
	
	public Key input() {
		// TODO Auto-generated method stub
		return inputKey;
	}
	
	public void processing() {
		// TODO Auto-generated method stub
		
		if(fd.getRemainingTime() > 0)
		{
			EnemyL = cc.getEnemyCharacter().getLeft();
			EnemyR = cc.getEnemyCharacter().getRight();	 
			Myhit = 0;
			Enemyhit = 0;
			
			if(cc.getskillFlag())
			{
				inputKey = cc.getSkillKey();
			}
			else
			{
			if(gd.getPlayerOneCharacterName().equals(CHARACTER_ZEN)){
				if((fd.getRemainingTime()<5000 && cc.getMyEnergy()>=300) || (fd.getRemainingTime()<5000 && cc.getMyEnergy()>=50) )
				{
					if(cc.getMyEnergy()>=300)
					{
						cc.commandCall("STAND_D_DF_FC");
					}
					if(fd.getRemainingTime() <2000 && cc.getDistanceX() < 50)
					{ 
						cc.commandCall("STAND_F_D_DFB");
					}
					if(fd.getRemainingTime() <2000)
					{
						cc.commandCall("STAND_D_DF_FB");
					}
				}
				if(cc.getEnemyCharacter().state.equals(State.AIR) && cc.getMyCharacter().state.equals(State.AIR)){
					if(cc.getMyEnergy() > 50 && cc.getDistanceX() > 80)
					{
					cc.commandCall("AIR_D_DF_FB");
					}
					else if(cc.getMyEnergy() > 5 && cc.getEnemyY() > cc.getMyY()){
						cc.commandCall("AIR_DB");
						
					}
					else
						cc.commandCall("B");
				}
				if(cc.getEnemyCharacter().state.equals(State.AIR)){
					if(cc.getDistanceY() < 75)
					{
					cc.commandCall("STAND_F_D_DFA");
				}
				}
				if(cc.getMyCharacter().state.equals(State.STAND)){
					if((cc.getDistanceX() > 300) && (cc.getDistanceX() < 550)){
						cc.commandCall("STAND_D_DF_FA");
						
					}
					else if(cc.getMyX() > 600){
						cc.commandCall("FOR_JUMP");
						
					}
					else if(cc.getMyX() < -100){
						cc.commandCall("FOR_JUMP");
						
					}
					
				}
				else
					if(cc.getDistanceX()<70 && cc.getMyCharacter().getY() == 335)
					{
						cc.commandCall("AIR_FA");
					}
					
					if(cc.getDistanceX()<100 && cc.getMyCharacter().getY() == 335)
					{
						cc.commandCall("CROUCH_B");
					}
					if(cc.getDistanceX()<145 && cc.getMyCharacter().getY() == 335)
					{
						cc.commandCall("CROUCH_FB");
					}
					if(cc.getDistanceX()<175 && cc.getMyCharacter().getY() == 335)
					{
						cc.commandCall("STAND_FB");
					}
				
				}
			}
				
			if(gd.getPlayerOneCharacterName().equals(CHARACTER_GARNET)){
				
				if((fd.getRemainingTime()<5000 && cc.getMyEnergy()>=300) || (fd.getRemainingTime()<5000 && cc.getMyEnergy()>=50) )
				{
					if(cc.getMyEnergy()>=300)
					{
						cc.commandCall("STAND_D_DF_FC");
					}
					if(fd.getRemainingTime() <2000 && cc.getDistanceX() < 50)
					{ 
						cc.commandCall("STAND_F_D_DFB");
					}
					if(fd.getRemainingTime() <2000)
					{
						cc.commandCall("STAND_D_DF_FB");
					}
				}
				if(cc.getEnemyCharacter().state.equals(State.AIR) && cc.getMyCharacter().state.equals(State.AIR)){
					if(cc.getMyEnergy() > 50 && cc.getDistanceX() > 80)
					{
					cc.commandCall("AIR_D_DF_FB");
					}
					else if(cc.getMyEnergy() > 5 && cc.getEnemyY() > cc.getMyY()){
						cc.commandCall("AIR_DB");
						
					}
					else
						cc.commandCall("B B B");
				}
				if(cc.getEnemyCharacter().state.equals(State.AIR)){
					if(cc.getDistanceY() < 75)
					{
					cc.commandCall("AIR_UB");
					}
				}
				if(cc.getMyCharacter().state.equals(State.STAND)){
					if((cc.getDistanceX() > 300) && (cc.getDistanceX() < 550)){
						cc.commandCall("STAND_D_DF_FA");
						
					}
					else if(cc.getMyX() > 600){
						cc.commandCall("FOR_JUMP");
						
					}
					else if(cc.getMyX() < -100){
						cc.commandCall("FOR_JUMP");
						
					}
					
				}
				else
					if(cc.getDistanceX()<70 && cc.getMyCharacter().getY() == 335)
					{
						cc.commandCall("AIR_FA");
					}
					
					if(cc.getDistanceX()<100 && cc.getMyCharacter().getY() == 335)
					{
						cc.commandCall("CROUCH_B");
					}
					if(cc.getDistanceX()<145 && cc.getMyCharacter().getY() == 335)
					{
						cc.commandCall("CROUCH_FB");
					}
					if(cc.getDistanceX()<175 && cc.getMyCharacter().getY() == 335)
					{
						cc.commandCall("STAND_FB");
					}
				
				}
			}
			
			if(gd.getPlayerOneCharacterName().equals(CHARACTER_LUD)){
				if((fd.getRemainingTime()<5000 && cc.getMyEnergy()>=300) || (fd.getRemainingTime()<5000 && cc.getMyEnergy()>=50) )
				{
					if(cc.getMyEnergy()>=300)
					{
						cc.commandCall("STAND_D_DF_FC");
					}
					if(fd.getRemainingTime() <2000 && cc.getDistanceX() < 50)
					{ 
						cc.commandCall("STAND_F_D_DFB");
					}
					if(fd.getRemainingTime() <2000)
					{
						cc.commandCall("STAND_D_DF_FB");
					}
				}
				if(cc.getEnemyCharacter().state.equals(State.AIR) && cc.getMyCharacter().state.equals(State.AIR)){
					if(cc.getMyEnergy() > 50 && cc.getDistanceX() > 80)
					{
					cc.commandCall("AIR_D_DF_FB");
					}
					else if(cc.getMyEnergy() > 5 && cc.getEnemyY() > cc.getMyY()){
						cc.commandCall("AIR_DB");
						
					}
					else
						cc.commandCall("B B B");
				}
				if(cc.getEnemyCharacter().state.equals(State.AIR)){
					if(cc.getDistanceY() < 75)
					{
					cc.commandCall("STAND_F_D_DFA");
					}
				}
				if(cc.getMyCharacter().state.equals(State.STAND)){
					if((cc.getDistanceX() > 300) && (cc.getDistanceX() < 550)){
						cc.commandCall("AIR_DA");
						
					}
					else if(cc.getMyX() > 600){
						cc.commandCall("FOR_JUMP");
						
					}
					else if(cc.getMyX() < -100){
						cc.commandCall("FOR_JUMP");
						
					}
					
				}
				else
					if(cc.getDistanceX()<70 && cc.getMyCharacter().getY() == 335)
					{
						cc.commandCall("AIR_FA");
					}
					
					if(cc.getDistanceX()<100 && cc.getMyCharacter().getY() == 335)
					{
						cc.commandCall("CROUCH_B");
					}
					if(cc.getDistanceX()<145 && cc.getMyCharacter().getY() == 335)
					{
						cc.commandCall("CROUCH_FB");
					}
					if(cc.getDistanceX()<175 && cc.getMyCharacter().getY() == 335)
					{
						cc.commandCall("STAND_FB");
					}
				
				}
		}
}