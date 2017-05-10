import enumerate.Action;
import enumerate.State;
import gameInterface.AIInterface;
import commandcenter.CommandCenter;
import java.util.LinkedList;
import java.util.Vector;

import simulator.Simulator;
import structs.CharacterData;
import structs.FrameData;
import structs.GameData;
import structs.Key;
import structs.MotionData;

public class Poring implements AIInterface {

  private Simulator simulator;
  private Key key;
  private CommandCenter commandCenter;
  private boolean playerNumber;
  private GameData gameData;

  /** Main FrameData */
  private FrameData frameData;

  /** Data with FRAME_AHEAD frames ahead of FrameData */
  private FrameData simulatorAheadFrameData;

  /** All actions that could be performed by self character */
  private LinkedList<Action> myActions;

  /** All actions that could be performed by the opponent character */
  private LinkedList<Action> oppActions;

  /** self information */
  private CharacterData myCharacter;

  /** opponent information */
  private CharacterData oppCharacter;

  /** Number of adjusted frames (following the same recipe in JerryMizunoAI) */
  private static final int FRAME_AHEAD = 14;

  private Vector<MotionData> myMotion;

  private Vector<MotionData> oppMotion;

  private Action[] actionAir;
  
  private Action[] LudList;
  
  private Action[] Nearby;
  
  private Action[] Escape;
  
  private Action [] LudFight;
  
  private Action[] actionGround;
  
  private Action[] GarnetFight;
  
  private Action[] goFace;

  private Action spSkill;

  private Node rootNode;

  /** True if in debug mode, which will output related log */
  public static final boolean DEBUG_MODE = false;

  @Override
  public void close() {}

  @Override
  public String getCharacter() {
    return CHARACTER_ZEN;
  }

  @Override
  public void getInformation(FrameData frameData) {
    this.frameData = frameData;
    this.commandCenter.setFrameData(this.frameData, playerNumber);

    if (playerNumber) {
      myCharacter = frameData.getP1();
      oppCharacter = frameData.getP2();
    } else {
      myCharacter = frameData.getP2();
      oppCharacter = frameData.getP1();
    }
  }

  @Override
  public int initialize(GameData gameData, boolean playerNumber) {
    this.playerNumber = playerNumber;
    this.gameData = gameData;	
    this.key = new Key();
    this.frameData = new FrameData();
    this.commandCenter = new CommandCenter();

    this.myActions = new LinkedList<Action>();
    this.oppActions = new LinkedList<Action>();

    simulator = gameData.getSimulator();

    actionAir =
        new Action[] {Action.AIR_GUARD, Action.AIR_A, Action.AIR_B, Action.AIR_DA, Action.AIR_DB,
            Action.AIR_FA, Action.AIR_FB, Action.AIR_UA, Action.AIR_UB, Action.AIR_D_DF_FA,
            Action.AIR_D_DF_FB, Action.AIR_F_D_DFA, Action.AIR_F_D_DFB, Action.AIR_D_DB_BA,
            Action.AIR_D_DB_BB};
    
    actionGround =
        new Action[] {Action.STAND_D_DB_BA, Action.FORWARD_WALK, Action.DASH,
            Action.JUMP, Action.FOR_JUMP, Action.BACK_JUMP, Action.STAND_GUARD,
            Action.THROW_B, Action.STAND_B,
            Action.CROUCH_A, Action.CROUCH_B, Action.STAND_FA, Action.STAND_FB, Action.CROUCH_FA,
            Action.CROUCH_FB, Action.STAND_D_DF_FA, Action.STAND_D_DF_FB, Action.STAND_F_D_DFA,
            Action.STAND_F_D_DFB, Action.STAND_D_DB_BB};
    
    spSkill = Action.STAND_D_DF_FC;
    
    GarnetFight =
    	new Action[] {Action.STAND_D_DB_BA, /*Action.FORWARD_WALK, Action.DASH,*/
            Action.JUMP, Action.FOR_JUMP, Action.BACK_JUMP, Action.STAND_GUARD,
            Action.THROW_B, Action.STAND_B,
            Action.STAND_FA, Action.STAND_FB, Action.CROUCH_FA,
            Action.CROUCH_FB, Action.STAND_D_DF_FA, /*Action.STAND_D_DF_FB,*/ Action.STAND_F_D_DFA,
            Action.STAND_F_D_DFB, Action.STAND_D_DB_BB};
    
    goFace =
    		new Action[] {
    				Action.FORWARD_WALK,Action.FOR_JUMP,
    				Action.DASH, Action.BACK_JUMP
    		};
    
    LudFight = 
    		new Action[] {Action.STAND_D_DB_BA, Action.FORWARD_WALK, Action.DASH,
            Action.JUMP, Action.BACK_JUMP, Action.STAND_GUARD,
            Action.STAND_B,
            Action.CROUCH_A, Action.CROUCH_B, Action.STAND_FB, /*Action.CROUCH_FA,*/
            Action.CROUCH_FB, Action.STAND_D_DF_FA, Action.STAND_D_DF_FB, Action.STAND_F_D_DFA,
            Action.STAND_D_DB_BB, Action.STAND_F_D_DFB};
    
    LudList	= 
    		new Action[] {Action.CROUCH_FB
    				};
    Nearby = 
    		new Action[] {Action.FORWARD_WALK};
    
    Escape = 
    		new Action[] {Action.FOR_JUMP, Action.STAND_GUARD,
    				Action.CROUCH_GUARD, Action.STAND_FA, Action.STAND_F_D_DFB,
    				Action.CROUCH_FB, Action.THROW_B};

    myMotion = this.playerNumber ? gameData.getPlayerOneMotion() : gameData.getPlayerTwoMotion();
    oppMotion = this.playerNumber ? gameData.getPlayerTwoMotion() : gameData.getPlayerOneMotion();

    return 0;
  }

  @Override
  public Key input() {
    return key;
  }

  @Override
  public void processing() {
    if (canProcessing()) {
      if (commandCenter.getskillFlag()) {
        key = commandCenter.getSkillKey();
      } else { // play
		  key.empty();
		  commandCenter.skillCancel();
    	  if(frameData.getRemainingTime() > 0 && gameData.getPlayerOneCharacterName().equals(CHARACTER_ZEN) && rulebased() == true)
    	  {      
    		  ZenRun();
    	  }
		  else {// mcTs
			  mctsPrepare(); // Some preparation for MCTS
			  rootNode =
					  new Node(simulatorAheadFrameData, null, myActions, oppActions, gameData, playerNumber,
                commandCenter);
			  rootNode.createNode();
			  Action bestAction = rootNode.mcts(); // Perform MCTS
			  if (Poring.DEBUG_MODE) 
			  {
				  rootNode.printNode(rootNode);
				  }
			  commandCenter.commandCall(bestAction.name()); // Perform an action selected by MCTS
			  } 
	  }
    }
  }

  /**
   * Determine whether or not the AI can perform an action
   *
   * @return whether or not the AI can perform an action
   */
  public boolean canProcessing() {
    return !frameData.getEmptyFlag() && frameData.getRemainingTime() > 0;
  }

  /**
   * Some preparation for MCTS
   * Perform the process for obtaining FrameData with 14 frames ahead
   */
  public void mctsPrepare() {
    simulatorAheadFrameData = simulator.simulate(frameData, playerNumber, null, null, FRAME_AHEAD);

    myCharacter = playerNumber ? simulatorAheadFrameData.getP1() : simulatorAheadFrameData.getP2();
    oppCharacter = playerNumber ? simulatorAheadFrameData.getP2() : simulatorAheadFrameData.getP1();

    setMyAction();
    setOppAction();
  }

  public void setMyAction() {
    myActions.clear();

    int energy = myCharacter.getEnergy();

    if (myCharacter.getState() == State.AIR) {
      for (int i = 0; i < actionAir.length; i++) {
        if (Math.abs(myMotion.elementAt(Action.valueOf(actionAir[i].name()).ordinal())
            .getAttackStartAddEnergy()) <= energy) {
          myActions.add(actionAir[i]);
        }
      }
    }
    if(gameData.getPlayerOneCharacterName().equals(CHARACTER_LUD)){
    	System.out.println("HP = : "+(commandCenter.getMyCharacter().hp - commandCenter.getEnemyCharacter().hp <= 0));
    	if(commandCenter.getDistanceX() <= 600 && commandCenter.getDistanceX() >= 370 && (commandCenter.getMyCharacter().hp - commandCenter.getEnemyCharacter().hp <= 0)){
    		for (int i = 0; i < Nearby.length; i++) {
    	        if (Math.abs(myMotion.elementAt(Action.valueOf(Nearby[i].name()).ordinal())
    	            .getAttackStartAddEnergy()) <= energy) {
    	          myActions.add(Nearby[i]);
    	          System.out.println("Nearby");
    	        }
    	      }
    	}
    	else if(commandCenter.getMyCharacter().getX() >= 680 || commandCenter.getMyCharacter().getX() == -170) {
    		for (int i = 0; i < Escape.length; i++) {
    	        if (Math.abs(myMotion.elementAt(Action.valueOf(Escape[i].name()).ordinal())
    	            .getAttackStartAddEnergy()) <= energy) {
    	          myActions.add(Escape[i]);
    	          System.out.println("Escpe");
    	        }
    	      }
    	}
    	else if(commandCenter.getMyCharacter().getX() >= 490 && commandCenter.getMyCharacter().getX() <= 620 && commandCenter.getDistanceX() <= 190 && commandCenter.getDistanceX() >= 75 || 
    			commandCenter.getMyCharacter().getX() <= 36 && commandCenter.getMyCharacter().getX() >= -170 && commandCenter.getDistanceX() <= 190 && commandCenter.getDistanceX() >= 75) {
    		for (int i = 0; i < LudList.length; i++) {
    	        if (Math.abs(myMotion.elementAt(Action.valueOf(Escape[i].name()).ordinal())
    	            .getAttackStartAddEnergy()) <= energy) {
    	          myActions.add(LudList[i]);
    	          System.out.println("Lud Test");
    	        }
    	      }
    	        }
    	else {
    		for (int i = 0; i < LudFight.length; i++) {
    	        if (Math.abs(myMotion.elementAt(Action.valueOf(LudFight[i].name()).ordinal())
    	            .getAttackStartAddEnergy()) <= energy) {
    	          myActions.add(LudFight[i]);
    	          System.out.println("LudFight");
    	          System.out.println(commandCenter.getDistanceX());
    	        }
    	      }
    	}
    }
    if(gameData.getPlayerOneCharacterName().equals(CHARACTER_GARNET)){
    	if(commandCenter.getDistanceX() <= 600 && commandCenter.getDistanceX() >= 370 && (commandCenter.getMyCharacter().hp - commandCenter.getEnemyCharacter().hp <= 0)){
    		for (int i = 0; i < goFace.length; i++) {
    	        if (Math.abs(myMotion.elementAt(Action.valueOf(goFace[i].name()).ordinal())
    	            .getAttackStartAddEnergy()) <= energy) {
    	          myActions.add(goFace[i]);
    	          System.out.println("goFace");
    	        }
    	      }
    	}
    	
    	else if(commandCenter.getMyCharacter().getX() >= 530 && commandCenter.getMyCharacter().getX() <= 620 && commandCenter.getDistanceX() <= 150 && commandCenter.getDistanceX() >= 50 || 
    			commandCenter.getMyCharacter().getX() <= -11 && commandCenter.getMyCharacter().getX() >= -170 && commandCenter.getDistanceX() <= 150 && commandCenter.getDistanceX() >= 50) {
    		for (int i = 0; i < LudList.length; i++) {
    	        if (Math.abs(myMotion.elementAt(Action.valueOf(Escape[i].name()).ordinal())
    	            .getAttackStartAddEnergy()) <= energy) {
    	          myActions.add(LudList[i]);
    	          System.out.println("Lud Test");
    	        }
    	      }
    	        }
    	else if(commandCenter.getEnemyCharacter().action.toString() == "STAND_D_DF_FB") {
    		for (int i = 0; i < LudList.length; i++) {
    	        if (Math.abs(myMotion.elementAt(Action.valueOf(Escape[i].name()).ordinal())
    	            .getAttackStartAddEnergy()) <= energy) {
    	          myActions.add(LudList[i]);
    	          System.out.println("Garnet Flee");
    	        }
    	      }
		  }
    	else
    	{
    		for (int i = 0; i < GarnetFight.length; i++) {
    	        if (Math.abs(myMotion.elementAt(Action.valueOf(GarnetFight[i].name()).ordinal())
    	            .getAttackStartAddEnergy()) <= energy) {
    	          myActions.add(GarnetFight[i]);
    	          System.out.println("Stage Out");
    	        }
    	        }
    		}    	
    	}
    }
  public boolean rulebased()
  {
	  System.out.println(commandCenter.getMyX());
	  if(commandCenter.getEnemyEnergy() <= 300 || commandCenter.getMyCharacter().action.toString().equals("STAND_D_DF_FC"))
	  {
		  return true;
	  }	  
	  else 
	  {
		  return false;
	  }
  }
  public void ZenRun() 
  {
	  if((frameData.getRemainingTime() < 10000 && commandCenter.getMyEnergy()>=300) || (frameData.getRemainingTime() < 10000 && commandCenter.getMyEnergy() >= 50) )
		{
			if(commandCenter.getMyEnergy() >= 300)
			{
				commandCenter.commandCall("STAND_D_DF_FC");
			}
			if(frameData.getRemainingTime() < 2000 && commandCenter.getDistanceX() < 50)
			{ 
				commandCenter.commandCall("STAND_F_D_DFB");
			}
			if(frameData.getRemainingTime() <2000)
			{
				commandCenter.commandCall("STAND_D_DF_FB");
			}
		}
		if(commandCenter.getEnemyCharacter().state.equals(State.AIR) && commandCenter.getMyCharacter().state.equals(State.AIR)){
			if(commandCenter.getMyEnergy() > 50 && commandCenter.getDistanceX() > 80)
				commandCenter.commandCall("AIR_D_DF_FB");
			else if(commandCenter.getMyEnergy() > 5 && commandCenter.getEnemyY() > commandCenter.getMyY()){
				commandCenter.commandCall("AIR_DB");
			}
			else
				commandCenter.commandCall("B");
		}
		if(commandCenter.getEnemyCharacter().state.equals(State.AIR) && !commandCenter.getMyCharacter().state.equals(State.DOWN) && !commandCenter.getMyCharacter().state.equals(State.AIR)){
			if(commandCenter.getDistanceY() < 50)
				commandCenter.commandCall("STAND_F_D_DFA");
		}
		if(commandCenter.getMyCharacter().state.equals(State.STAND)){
			if((commandCenter.getDistanceX() > 300) && (commandCenter.getDistanceX() < 550)){
				commandCenter.commandCall("STAND_D_DF_FA");
			}
			else if(commandCenter.getMyX() >= 680){
				commandCenter.commandCall("FOR_JUMP");
			}
			else if(commandCenter.getMyX() <= -120){
				commandCenter.commandCall("FOR_JUMP");
			}
			else
				commandCenter.commandCall("FOR_JUMP");
		}
		else
			commandCenter.commandCall("B");
		
		}  
  public void setOppAction() {
    oppActions.clear();

    int energy = oppCharacter.getEnergy();

    if (oppCharacter.getState() == State.AIR) {
      for (int i = 0; i < actionAir.length; i++) {
        if (Math.abs(oppMotion.elementAt(Action.valueOf(actionAir[i].name()).ordinal())
            .getAttackStartAddEnergy()) <= energy) {
          oppActions.add(actionAir[i]);
        }
      }
    } else {
      if (Math.abs(oppMotion.elementAt(Action.valueOf(spSkill.name()).ordinal())
          .getAttackStartAddEnergy()) <= energy) {
        oppActions.add(spSkill);
      }

      for (int i = 0; i < actionGround.length; i++) {
        if (Math.abs(oppMotion.elementAt(Action.valueOf(actionGround[i].name()).ordinal())
            .getAttackStartAddEnergy()) <= energy) {
          oppActions.add(actionGround[i]);
        }
      }
    }
  }
}