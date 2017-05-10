import enumerate.Action;
import enumerate.State;
import gameInterface.AIInterface;

import java.util.LinkedList;
import java.util.Vector;

import simulator.Simulator;
import structs.CharacterData;
import structs.FrameData;
import structs.GameData;
import structs.Key;
import structs.MotionData;

import commandcenter.CommandCenter;

/**
 *
 * @Author Jaykim
 * @reference Taichi's MCTS
 */
public class JayBot2016 implements AIInterface {

  private Simulator simulator;
  private Key key;
  private boolean playerNumber;
  private GameData gameData;
  private CommandCenter cc;

  
  /** 大本のFrameData */
  private FrameData frameData;

  /** 大本よりFRAME_AHEAD分遅れたFrameData */
  private FrameData simulatorAheadFrameData;

  /** 自分が行える行動全て */
  private LinkedList<Action> myActions;

  /** 相手が行える行動全て */
  private LinkedList<Action> oppActions;

  /** 自分の情報 */
  private CharacterData myCharacter;

  /** 相手の情報 */
  private CharacterData oppCharacter;

  /** フレームの調整用時間(JerryMizunoAIを参考) */
  private static final int FRAME_AHEAD = 14;

  private Vector<MotionData> myMotion;

  private Vector<MotionData> oppMotion;

  private Action[] actionAir;

  private Action[] actionGround;
  
  private Action spSkill;

  private Node rootNode;
  
  private int CN;

  /** デバッグモードであるかどうか。trueの場合、様々なログが出力される */
  
  // whether Combo System is Hit and NonHit.
  // 상대가 대처를 하는지에 대해 학습이 가능한가?
  // 콤보가 완전히 깨지는지에 대해 봐야한다.
  // 초반에는 리워드값을 비율로 타격을 일정이상 당하면 리워드를 점수로 변경
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
    this.cc.setFrameData(this.frameData, this.playerNumber);
   
    if (this.playerNumber) {
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
    this.cc = new CommandCenter();

    this.myActions = new LinkedList<Action>();
    this.oppActions = new LinkedList<Action>();
    

    simulator = gameData.getSimulator();

    actionAir =
        new Action[] {Action.AIR_GUARD, Action.AIR_A, Action.AIR_B, Action.AIR_DA, Action.AIR_DB,
            Action.AIR_FA, Action.AIR_FB, Action.AIR_UA, Action.AIR_UB, Action.AIR_D_DF_FA,
            Action.AIR_D_DF_FB, Action.AIR_F_D_DFA, Action.AIR_F_D_DFB, Action.AIR_D_DB_BA,
            Action.AIR_D_DB_BB};
    actionGround =
        new Action[] {Action.STAND_D_DB_BA, Action.BACK_STEP, Action.FORWARD_WALK, Action.DASH,
            Action.JUMP, Action.FOR_JUMP, Action.BACK_JUMP, Action.STAND_GUARD,
            Action.CROUCH_GUARD, Action.THROW_A, Action.THROW_B, Action.STAND_A, Action.STAND_B,
            Action.CROUCH_A, Action.CROUCH_B, Action.STAND_FA, Action.STAND_FB, Action.CROUCH_FA,
            Action.CROUCH_FB, Action.STAND_D_DF_FA, Action.STAND_D_DF_FB, Action.STAND_F_D_DFA,
            Action.STAND_F_D_DFB, Action.STAND_D_DB_BB};
   
    spSkill = Action.STAND_D_DF_FC;//필살기

    myMotion = this.playerNumber ? gameData.getPlayerOneMotion() : gameData.getPlayerTwoMotion();
    oppMotion = this.playerNumber ? gameData.getPlayerTwoMotion() : gameData.getPlayerOneMotion();
    System.out.println("성훈4");
    return 0;
  }

  @Override
  public Key input() {
    return key;
  }

  @Override
  public void processing() {

    if (canProcessing()) {
      if (cc.getskillFlag()) {
        key = cc.getSkillKey();
      } else {
        key.empty();
        cc.skillCancel();

        mctsPrepare(); // MCTS준비작업
        rootNode =
            new Node(simulatorAheadFrameData, null, myActions, oppActions, gameData, playerNumber,
                cc);
        rootNode.createNode();

        Action bestAction = rootNode.mcts(); // MCTS실행
        if (JayBot2016.DEBUG_MODE) {
          rootNode.printNode(rootNode);
        }
        System.out.println("My Action: "+bestAction.name());

        cc.commandCall(bestAction.name()); // MCTS 선택 액션 진행
      }
    }
  }

  /**
   * AI가 행동가능한지 확인
   *
   * @return AI행동 가능여부를 돌려줌
   */
  public boolean canProcessing() {
    return !frameData.getEmptyFlag() && frameData.getRemainingTime() > 0;
  }

  /**
   * MCTS사전 준비 <br>
   * 14フレーム進ませたFrameDataの取得などを行う
   * 14프레임 지연 시켰다가 FrameData취득 등을 실시한다
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

    if (myCharacter.getState() == State.AIR) {//내가 공중일때
      for (int i = 0; i < actionAir.length; i++) {//공중 15가지 행동을 돌리고
        if (Math.abs(myMotion.elementAt(Action.valueOf(actionAir[i].name()).ordinal())
            .getAttackStartAddEnergy()) <= energy) {//내가 가진 에너지를 고려해서 돌릴수 있는 스킬들만 액션에 추가한다.
        	myActions.add(actionAir[i]);
          cc.skillCancel();
        
        }
      }
    } else {//공중이 아닐때
      if (Math.abs(myMotion.elementAt(Action.valueOf(spSkill.name()).ordinal())
          .getAttackStartAddEnergy()) <= energy) {//에너지가 필살기 쓸수있으면 써라
    	    myActions.add(spSkill);
    	    cc.skillCancel();
      }

      if(gameData.getPlayerTwoCharacterName()=="ZEN"){
      for (int i = 0; i < actionGround.length; i++) {//내가 지상일때는
        if (Math.abs(myMotion.elementAt(Action.valueOf(actionGround[i].name()).ordinal())
            .getAttackStartAddEnergy()) <= energy&&cc.getDistanceX()<50) {//내가가진 24개의 기술을 에너지를 고려해서 쓸수 있는것만 액션에 넣는다.
          
        		myActions.add(Action.CROUCH_FA);
        	}
        if (Math.abs(myMotion.elementAt(Action.valueOf(actionGround[i].name()).ordinal())
                .getAttackStartAddEnergy()) <= energy&&cc.getDistanceX()<85) {//내가가진 24개의 기술을 에너지를 고려해서 쓸수 있는것만 액션에 넣는다.
             
        		myActions.add(Action.CROUCH_A);
        		
        	}
        if (Math.abs(myMotion.elementAt(Action.valueOf(actionGround[i].name()).ordinal())
                .getAttackStartAddEnergy()) <= energy&&cc.getDistanceX()<100) {//내가가진 24개의 기술을 에너지를 고려해서 쓸수 있는것만 액션에 넣는다.
              
        		
        		myActions.add(Action.STAND_FA);
        	}
        if (Math.abs(myMotion.elementAt(Action.valueOf(actionGround[i].name()).ordinal())
                .getAttackStartAddEnergy()) <= energy&&cc.getDistanceX()<105) {//내가가진 24개의 기술을 에너지를 고려해서 쓸수 있는것만 액션에 넣는다.

    		myActions.add(Action.CROUCH_FB);
        }
        
        	myActions.add(actionGround[i]);
          cc.skillCancel();
        }
      }
      else
    	  for (int i = 0; i < actionGround.length; i++) {//내가 지상일때는
    	  myActions.add(actionGround[i]);
    	  
      }
      }
    }


  public void setOppAction() {
    oppActions.clear();

    int energy = oppCharacter.getEnergy();

    if (oppCharacter.getState() == State.AIR) {
      for (int i = 0; i < actionAir.length; i++) {
        if (Math.abs(oppMotion.elementAt(Action.valueOf(actionAir[i].name()).ordinal())
            .getAttackStartAddEnergy()) <= energy) {//상대의 공중상황도 마찬가지로 확인
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
