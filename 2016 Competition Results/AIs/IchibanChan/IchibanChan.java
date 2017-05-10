import commandcenter.CommandCenter;
import enumerate.State;
import gameInterface.AIInterface;
import simulator.Simulator;
import structs.FrameData;
import structs.GameData;
import structs.Key;
import structs.CharacterData;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
//import java.util.Random;
import java.util.SplittableRandom;

public class IchibanChan implements AIInterface {

	Key inputKey;
	boolean playerNumber;
	FrameData frameData;
	CommandCenter cc;
	GameData gd;
	Simulator simulator;
	//Random rndGenerator;
	SplittableRandom rndGenerator;
	
	//gameVariables
	private int distance; //distance to opponent
	private int myEnergy; // own energy
	private int enemyEnergy; // enemy energy
	private int wallSituation; //0 = I am between enemy and wall, 1 = enemy is between me and wall, 2 = no danger
	private int distanceSituation; //0 = contact, 1 = near, 2 = middle, 3 = far
	private CharacterData myself; //my player data
	private CharacterData enemy; //enemy player data
	private boolean onStart;
	private int randomValue; //randomValue that is newly assigned for every action
	private int deadLockTime = 3500; //check for deadLock every x seconds
	private long dlCheckStart = 0; //start Time of deadLock check = remaining game time
	private boolean doDeadLockBreak = false;
	private int[] deadLockData = {0,0,0}; //comparison data to check for deadlock, {distance,myHP,enemyHP}
	
	//Character_Settings
	//Distances {contactDistance,nearDistance,midDistance,comfortableWallDistance}
	static final Map<String,int[]> CHARACTER_DISTANCES;
	static{
		final Map<String,int[]> characterDistances = new HashMap<>();
		//Zen Distances
		characterDistances.put("ZEN", new int[]{50,150,320,200});
		//Garnet Distances
		characterDistances.put("GARNET", new int[]{50,150,320,200});
		//Lud Distances
		characterDistances.put("LUD", new int[]{50,150,320,200});
		CHARACTER_DISTANCES = Collections.unmodifiableMap(characterDistances);
	}
	
	//Attacks
	//Attacks {fastDownKick,specialDownKick,airBreakAttack,midKick,superAttack,ultimateAttack}
	static final Map<String,String[]> CHARACTER_ATTACKS;
	static{
		final Map<String,String[]> characterAttacks = new HashMap<>();
		//Zen Attacks
		characterAttacks.put("ZEN", new String[]{"CROUCH_B","CROUCH_FB","STAND_F_D_DFA","B","STAND_D_DB_BB","STAND_D_DF_FC"});
		//Garnet Attacks
		characterAttacks.put("GARNET", new String[]{"CROUCH_B","CROUCH_FB","CROUCH_FA","STAND_FB","STAND_D_DB_BB","STAND_D_DF_FC"});
		//Lud Attacks
		characterAttacks.put("LUD", new String[]{"CROUCH_B","CROUCH_FB","STAND_F_D_DFA","B","STAND_D_DB_BB","STAND_D_DF_FC"});				
		CHARACTER_ATTACKS = Collections.unmodifiableMap(characterAttacks);
	}
	
	//AI settings
	private int midDistance;
	private int nearDistance;
	private int contactDistance;
	private int comfortableWallDistance;
	private int criticalEnemyEnergy = 300;
	private String fastDownKick;
	private String specialDownKick;
	private String airBreakAttack;
	private String midKick;
	private String ultimateAttack;
	private String specialAttack;
	
	
	@Override
	public String getCharacter() {
		return gd.getMyName(playerNumber);
	}
	
	@Override
	public void close() {
		//do nothing
	}

	@Override
	public void getInformation(FrameData frameData) {
		this.frameData = frameData;
		cc.setFrameData(this.frameData, playerNumber);

	}

	@Override
	public int initialize(GameData gameData, boolean playerNumber) {
		this.playerNumber = playerNumber;
		this.inputKey = new Key();
		gd = gameData;
		cc = new CommandCenter();
		frameData = new FrameData();
		rndGenerator = new SplittableRandom();
		simulator = gd.getSimulator();
		onStart = true;
		setAISettings(gd.getMyName(playerNumber));		
		return 0;
	}

	@Override
	public Key input() {
		return inputKey;
	}

	@Override
	public void processing() {
		//if game is running
		if(!frameData.getEmptyFlag() && frameData.getRemainingTime() > 0) {
			
			//simulate 17 frames ahead
			//15 for input delay
			//+ 2 since the fastest attack requires 3 frames
			//so we know which move might be best, when the next move is being performed
			if(!onStart){
				frameData = simulator.simulate(frameData, this.playerNumber, null, null, 17);
			}
			else{
				onStart = false;
			}
			
			cc.setFrameData(frameData, playerNumber);
			
			//get gameVariables
			distance = cc.getDistanceX();
			myEnergy = cc.getMyEnergy();
			enemyEnergy = cc.getEnemyEnergy();
			myself = cc.getMyCharacter();
			enemy = cc.getEnemyCharacter();
			
			//keep doing action if action is in progress
			if(cc.getskillFlag()){
				inputKey = cc.getSkillKey();
			}
			else{
				//clear actions
				inputKey.empty();
				cc.skillCancel();
				
				//check for possible deadLock
				checkDeadLock();
				
				if(doDeadLockBreak) {
					cc.commandCall(chooseAction(new String[]{"FOR_JUMP _B", "BACK_JUMP _B"}, new int[]{70}));					
				}
				//if in Air or Down, try to be quick
				else if(myself.state.equals(State.AIR) || myself.state.equals(State.DOWN)){
				//if(myself.state.equals(State.DOWN)){
					if(myEnergy >= 50){
						cc.commandCall(chooseAction(new String[]{midKick, "2 _ B ", "2 1 4 _ B" }, new int[]{50, 30}));
					}
					else{
						cc.commandCall(chooseAction(new String[]{midKick, "2 _ B"}, new int[]{80}));
					}
				}
				//try to be clever
				else{

					//check Wall and act accordingly
					wallSituation = checkWall();
					
					//not near Wall, no Danger
					if(wallSituation > 1){
						handleDistance();
					}
					//try to keep enemy at wall
					else if(wallSituation > 0){
						if(enemyEnergy >= criticalEnemyEnergy){
							//try to avoid enemy super attack
							cc.commandCall(chooseAction(new String[]{"FOR_JUMP _B", midKick}, new int[]{70}));
						}
						else{
							cc.commandCall(chooseAction(new String[]{specialDownKick, midKick}, new int[]{50}));
						}
					}
					// between wall and enemy
					else{
						if(myEnergy >= 50 && myEnergy < 250 && enemyEnergy < 285){
							//try to escape with slide
							cc.commandCall(chooseAction(new String[]{midKick, specialAttack }, new int[]{70}));
						}
						//try to escape with jump
						cc.commandCall("FOR_JUMP _B");
					}
				}

			}
		}
	}
	
	//check if player is too close to wall
	private int checkWall(){
		//check right wall
		if((gd.getStageXMax() - myself.right) < comfortableWallDistance && distance < nearDistance){
			//if I am between enemy and wall
			if(myself.right >= enemy.right){
				return 0;
			}
			//enemy is between me and wall, yeah!
			return 1;
		}
		//check left wall
		if(myself.left < comfortableWallDistance && distance < nearDistance){
			//if I am between enemy and wall
			if(myself.left <= enemy.left){
				return 0;
			}
			//enemy is between me and wall, yeah!
			return 1;
		}
		//no wall near me!
		return 2;
	}

	//check distance of players depending on threshold
	private int checkDistance(){
		//far away
		if(distance >= midDistance){
			return 3;
		}
		//mid range
		if(distance >= nearDistance){
			return 2;
		}
		//near range
		if(distance >=  contactDistance){
			return 1;
		}
		//contact range
		return 0;
		}

	//handle distances accordingly
	private void handleDistance(){
		//check Distance and act accordingly
		distanceSituation = checkDistance();
		//far away
		if(distanceSituation > 2){
			handleFar();
		}
		//mid range
		else if(distanceSituation > 1){
			handleMid();
		}
		//near range
		else if(distanceSituation > 0){
			handleNear();
		}
		//contact range
		else {
			handleContact();
		}
	}
	
	//choose action from actions[] according to probabilities from probs[]
	private String chooseAction(String[] actions, int[] probs){
		//randomValue = rndGenerator.nextInt(100);
		randomValue = rndGenerator.nextInt(100);
		for(int i = 0; i < probs.length; i++){
			if(randomValue<=probs[i]){
				return actions[i];
			}
			randomValue -= probs[i];
		}
		return actions[actions.length-1];
	}
	
	// handles actions for far range, generally minimize distance
	private void handleFar(){
		if(enemy.state.equals(State.AIR)){
			cc.commandCall(chooseAction(new String[]{"DASH", "DASH STAND_F_D_DFA"}, new int[]{70}));
		}
		else{
			cc.commandCall(chooseAction(new String[]{"FOR_JUMP _B", "DASH"}, new int[]{50}));
		}
	}
	
	// handles actions for mid range
	private void handleMid(){
		if(enemy.state.equals(State.AIR)){
			//beat in the air towards jumping enemy or just minimize distance
			cc.commandCall(chooseAction(new String[]{airBreakAttack, "DASH"}, new int[]{80}));
		}
		else{
			if(enemyEnergy >= criticalEnemyEnergy){
				//try to avoid enemy super attack
				cc.commandCall("FOR_JUMP _B");
			}
			else if(myEnergy >= 300){
				//perform super attack
				cc.commandCall(ultimateAttack);
			}
			else{
				//minimize distance for close combat
				cc.commandCall(chooseAction(new String[]{"DASH", "FOR_JUMP _B"}, new int[]{70}));
			}
		}
	}
	
	// handles actions for near range
	private void handleNear() {
		if(enemy.state.equals(State.AIR)){
			//hit in the air, when enemy tries to escape the near range with a jump
			cc.commandCall(airBreakAttack);
		}
		else {
			if(enemyEnergy >= criticalEnemyEnergy){
				//try to avoid enemy super attack
				cc.commandCall("FOR_JUMP _B");
			}
			//just kick and if enough energy you may slide
			else if(myEnergy >= 50 && myEnergy < 250 && enemyEnergy < 285){
				cc.commandCall(chooseAction(new String[]{midKick, specialAttack }, new int[]{80}));
			}
			else{
				cc.commandCall(chooseAction(new String[]{midKick, fastDownKick}, new int[]{70}));
			}
		}
	}
	
	// handles actions for contact range
	private void handleContact(){
		if(enemy.state.equals(State.AIR)){
			//hit in the air, when enemy tries to escape the contact range with a jump
			cc.commandCall(airBreakAttack);
		}
		else if(enemyEnergy >= criticalEnemyEnergy){
				//try to avoid enemy super attack
			cc.commandCall(chooseAction(new String[]{"FOR_JUMP _B", midKick}, new int[]{70}));
		}
		else{ 
			//don't move, just kick
			cc.commandCall(chooseAction(new String[]{midKick, fastDownKick}, new int[]{60}));
		}
	}
	
	//set AI Settings according to character name
	private void setAISettings(String name){
		int[] useDistances = CHARACTER_DISTANCES.get(name);
		midDistance = useDistances[2];
		nearDistance = useDistances[1];
		contactDistance = useDistances[0];
		comfortableWallDistance = useDistances[3];
		
		String[] useAttacks = CHARACTER_ATTACKS.get(name);
		fastDownKick = useAttacks[0];
		specialDownKick = useAttacks[1];
		airBreakAttack = useAttacks[2];
		midKick = useAttacks[3];
		ultimateAttack = useAttacks[5];
		specialAttack = useAttacks[4];
	}
	
	
	// check if players are stuck
	private void checkDeadLock(){
		if(dlCheckStart - deadLockTime >= frameData.getRemainingTime() && !doDeadLockBreak){					
			//possibly deadLock
			System.out.println("DEADLOCK!");
			if(myself.getHp() <= enemy.getHp()){
				//in this case might not win
				//break free from deadLock
				doDeadLockBreak = true;
			}
			//if not: we are in the lead and try to abuse the situation by doing nothing
		}
		else if(deadLockData[0] != distance || deadLockData[1] != myself.getHp() || deadLockData[2] != enemy.getHp()){
			//if not, can not be deadLock
			//reset deadLock check anyway
			dlCheckStart = frameData.getRemainingTime();
			deadLockData[0] = distance; //remember distance
			deadLockData[1] = myself.getHp(); //remember my health
			deadLockData[2] = enemy.getHp(); //remember enemy health
			doDeadLockBreak = false;
		}
	}
	
}
