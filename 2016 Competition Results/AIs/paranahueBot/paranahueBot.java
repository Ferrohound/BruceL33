import gameInterface.AIInterface;
import structs.FrameData;
import structs.GameData;
import structs.Key;
import simulator.Simulator;
import commandcenter.CommandCenter;
import enumerate.State;
import structs.CharacterData;


public class iaTest implements AIInterface {

	private Key inputKey;
	private boolean player;
	private FrameData frameData;
	private CommandCenter cc;
	private Simulator simulator;
	private GameData gd;
	
	private int distance;
	private int distanceY;
	private int energy;
	private CharacterData opp;
	private CharacterData my;
	private boolean isGameJustStarted;
	private int xDifference;
	int enemySpeedX;
	int enemySpeedY;
	String oppmotion;
	boolean p;
	boolean playerNumber;
	String MySkillName, OppSkillName;
	
	
	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getCharacter() {
		// TODO Auto-generated method stub
		// Select the player ZEN as per competition rules
		return CHARACTER_ZEN;
	}

	@Override
	public void getInformation(FrameData frameData) {
		// TODO Auto-generated method stub
		this.frameData = frameData;
	}

	@Override
	public int initialize(GameData arg0, boolean player) {
		// TODO Auto-generated method stub
		inputKey = new Key();
		this.player = player;
		frameData = new FrameData();
		cc = new CommandCenter();	
		gd = arg0;
		simulator = gd.getSimulator();
		isGameJustStarted = true;
		xDifference = -300;
		p = player;
		return 0;
	}

	@Override
	public Key input() {
		// TODO Auto-generated method stub
		return inputKey;
	}

	@Override
	public void processing() {
		// First we check whether we are at the end of the round
		if(!frameData.getEmptyFlag() && frameData.getRemainingTime()>0){
			// Simulate the delay and look ahead 2 frames. The simulator class exists already in FightingICE
			if (!isGameJustStarted)
				frameData = simulator.simulate(frameData, this.player, null, null, 17);
			else
				isGameJustStarted = false; //if the game just started, no point on simulating
			cc.setFrameData(frameData, player);
			distance = cc.getDistanceX();
			distanceY = cc.getDistanceY();
			energy = frameData.getMyCharacter(player).getEnergy();
			my = cc.getMyCharacter();
			opp = cc.getEnemyCharacter();
			xDifference = my.x - opp.x;
			enemySpeedX = cc.getEnemyCharacter().getSpeedX();
			enemySpeedY = cc.getEnemyCharacter().getSpeedY();
			
			OppSkillName = cc.getEnemyCharacter().action.name().toString();
			MySkillName = cc.getMyCharacter().action.name().toString();
		
			
			
			if (cc.getskillFlag()) {
				// If there is a previous "command" still in execution, then keep doing it
				inputKey = cc.getSkillKey();
			}
			else {
				// We empty the keys and cancel skill just in case
				inputKey.empty(); 
				cc.skillCancel();
				// Following is the brain of the reflex agent. It determines distance to the enemy
				// and the energy of our agent and then it performs an action
				if ((opp.energy >= 2000) && ((my.hp - opp.hp) <= 300))
					cc.commandCall("FOR_JUMP _B B B");
					// if the opp has 300 of energy, it is dangerous, so better jump!!
					// if the health difference is high we are dominating so we are fearless :)
				else if (!my.state.equals(State.AIR) && !my.state.equals(State.DOWN)) { //if not in air
					if ((distance > 150 && !OppSkillName.contains("THROW_SUFFER")) || ((cc.getMyX() < -100 && my.isFront() == true) || (cc.getMyX() > 660 && my.isFront() == false))) {
						cc.commandCall("FOR_JUMP"); //If its too far, then jump to get closer fast
					}
					else if (energy >= 300 && (OppSkillName.contains("STAND_F_D_DFA") || (OppSkillName.contains("THROW_SUFFER") &&  (opp.energy < 200))))
						cc.commandCall("STAND_D_DF_FC"); //High energy projectile
					else if (distance > 99 && OppSkillName.contains("STAND_F_D_DFA") && energy >= 50)
						cc.commandCall("STAND_D_DB_BB"); //Perform a slide kick punish
					else if ((distance > 130) && (energy < 30) && OppSkillName.contains("THROW_SUFFER"))
						//cc.commandCall("6 6 6 STAND_D_DB_BB"); //Perform a slide kick com 50 de energia
						cc.commandCall("BACK_STEP 5 5 5 5 5 STAND_D_DF_FA");
					else if ((distance > 130) && (energy >= 30) && OppSkillName.contains("THROW_SUFFER"))
						//cc.commandCall("6 6 6 STAND_D_DB_BB"); //Perform a slide kick com 50 de energia
						cc.commandCall("BACK_STEP 5 5 5 5 5 STAND_D_DF_FB");
					//else if ((distance > 99) && (energy >= 50) && (!opp.state.equals(State.AIR)) && !OppSkillName.contains("STAND_D_DB_BB") && !OppSkillName.contains("B"))
						//cc.commandCall("STAND_D_DB_BB"); //Perform a slide kick
					//else if ((distance > 99) && OppSkillName.contains("STAND_D_DB_BB") && (!opp.state.equals(State.AIR)))
						//cc.commandCall("7"); //Perform a slide kick
					else if ((distance <= 94) && (distanceY <= 175) /*&& (enemySpeedY >= 0)*/ && (energy >= 20) && opp.state.equals(State.AIR) && OppSkillName.contains("THROW_SUFFER")) {
						cc.commandCall("THROW_B");
//						System.out.println("distance : " + distance + " action : " + OppSkillName);	
					}
					//else if (distance > 150 && (energy >= 20) && opp.state.equals(State.AIR) && !opp.state.equals(State.DOWN) && (enemySpeedY < 0))
					//cc.commandCall("STAND_F_D_DFB");
					else if ((distance > 89) && opp.state.equals(State.AIR) && (energy >= 20) && OppSkillName.contains("THROW_SUFFER"))
						cc.commandCall("6"); // Perform a quick dash to get closer
					//else if ((distance >= 100) && (distance <= 130) && OppSkillName.contains("STAND") && !opp.state.equals(State.AIR) && opp.energy < 50)
						//cc.commandCall("STAND_FB");
					else if ((distance > 100) && !opp.state.equals(State.AIR) && !OppSkillName.contains("B") && opp.energy < 50)
						cc.commandCall("6 6"); // Perform a quick dash to get closer
					else if ((distance <= 64) && (energy >= 5)) //menor distância possível
						cc.commandCall("THROW_A");
					else if (distance <= 100 && opp.state.equals(State.AIR) && !OppSkillName.contains("THROW_SUFFER") && cc.getMyCharacter().isFront() == true) //if enemy on Air
						cc.commandCall("STAND_F_D_DFA"); //Perform a big punch menor distância horizontal possível
					else if (!opp.state.equals(State.AIR) && (distance <= 99) && opp.energy < 40)
						cc.commandCall("B"); //Perform a kick in all other cases, introduces randomness
					else if ((distance >= 80) && OppSkillName.contains("THROW_SUFFER"))
						cc.commandCall("6");
					else {
						cc.commandCall("9 AIR _FB");
						System.out.println("distanceX : " + distance + " distanceY : " + distanceY + " Oppaction : " + OppSkillName + " MyAction : " + MySkillName);	
					}
				}
				else if ((distance <= 150) && (my.state.equals(State.AIR) || my.state.equals(State.DOWN)) 
						&& (((gd.getStageXMax() - my.x)>=200) || (xDifference > 0))
						&& ((my.x >=200) || xDifference < 0)) { //Conditions to handle game corners
					if (energy >= 5) 
						cc.commandCall("AIR_DB"); // Perform air down kick when in air
					else
						cc.commandCall("B"); //Perform a kick in all other cases, introduces randomness
				}	
				else
					cc.commandCall("AIR _FB"); //Perform a kick in all other cases, introduces randomness
			}
		}
		else isGameJustStarted = true;
	}
}

