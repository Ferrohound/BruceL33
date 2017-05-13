import java.util.Enumeration;
import java.util.Hashtable;

import commandcenter.CommandCenter;
import enumerate.Action;
import gameInterface.AIInterface;
import simulator.Simulator;
import structs.FrameData;
import structs.GameData;
import structs.Key;

/* After initialization, the process will continue as
	1.	getInformaiton
	2.	processing
	3.	input
	for each frame.
 */

/*
 * Useful classes
 * Action
 * FrameData
 * CharacterData
 * MotionData
 * 
 */

public class BruceL33 implements AIInterface {
	
	Key inputKey;
	FrameData frameData;
	boolean player;
	CommandCenter cc;
	GameData gameData;
	Simulator simulator;
	
	
	Hashtable<String, Hashtable<String, Float>> Q_Table;
	Hashtable<String, Hashtable<String, Float>> R_Table;
	
	boolean gameJustStarted = false;
	
	String state;
	String prevState;
	String prevAction;
	int prevHP;
	int prevEnemyHP;
	
	
	float init_q_value = 0.0f;
	float init_r_value = 0.0f;
	int downed_self_reward = -2;
	int downed_enemy_reward = 2;
	
	int hp_grouping = 20;
	int energy_grouping = 10;
	int dist_x_grouping = 3;
	int dist_y_grouping = 10;
	int distance_cut_off = 250;
	
	float alpha = 0.8f;
	float gamma = 0.2f;
	float epsilon = 0.3f;
	
	/*
	 * This method finalizes AI. It runs only once at the end of each game.
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub

	}
	/*
	 * This method is for deciding which character to use among ZEN, GARNET, LUD, and KFM, and 
	 * it returns one of the following values, which must be specified after "return" for the competition:
	CHARACTER_ZEN, CHARACTER_GARNET, CHARACTER_LUD, and CHARACTER_KFM
	Note that ZEN, GARNET, and LUD are the only official characters in the 2017 competition, 
	but only the motion data of the first two are the official ones; LUD's motion data are non-official and will be changed in the 
	competition.
	 */
	@Override
	public String getCharacter() {
		// TODO Auto-generated method stub
		return "ZEN";
	}
	
	/*
	 * This method gets information from the game status of each frame. 
	 * Such information is stored in the parameter fd. 
	 * If fd.getRemaningTime() returns a negative value, the current round has not started yet.
	 * 
	 * 
	 * When you use frameData received from getInformation(), you must always check 
	 * if the condition "!frameData.emptyFlag && frameData.getRemainingTime() > 0" holds; 
	 * otherwise, NullPointerException will occur. 
	 */
	
	public void reset()
	{
		prevState = "";
		prevAction = "";
		
	}
	
	@Override
	public void getInformation(FrameData arg0) {
		// TODO Auto-generated method stub
		frameData = arg0;
		cc.setFrameData(frameData, player);
		
	}

	/*
	 * This method initializes AI, and it will be executed only once in the beginning of a game. 
	 * Its execution will load the data that cannot be changed and load the flag of player's side ("boolean player", 
	 * true for P1 or false for P2)
If there is anything that needs to be initialized, you had better do it in this method. It will return 0 when such
 initialization finishes correctly, otherwise the error code.
	 */
	@Override
	public int initialize(GameData arg0, boolean arg1) {
		Q_Table = new Hashtable<String, Hashtable<String, Float>>();
		R_Table = new Hashtable<String, Hashtable<String, Float>>();
		
		inputKey = new Key();
		this.player = player;
		frameData = new FrameData();
		this.gameData = gameData;
		this.cc = new CommandCenter();
	    simulator = gameData.getSimulator();
	    
	    reset();
		
		// TODO Auto-generated method stub
		return 0;
	}
	
	/*
	 *The input method receives key inputted from AI. It is executed in each frame and returns a value in the Key type. 
	Key has the following instance fields:
	boolean U
	boolean D
	boolean L
	boolean R
	boolean A
	boolean B
	boolean C
	The instance-field U, D, L, and R represent the direction key inputted by the player using the numeric keypad. 
	They are also used in a combination with the instance-field A, B, and C for generating a skill.
	 */

	@Override
	public Key input() {
		// TODO Auto-generated method stub
		return inputKey;
	}

	/*
	 * This method processes the data from AI. It is executed in each frame.
	 */
	@Override
	public void processing() {
		// TODO Auto-generated method stub
		
		if(frameData.getEmptyFlag()||frameData.getRemainingTimeMilliseconds()<=0)
		{
			gameJustStarted = true;
			reset();
		}
		
		if(cc.getSkillFlag())
		{
			inputKey = cc.getSkillKey();
		}
		
		inputKey.empty();
		cc.skillCancel();
		
		state = encodeState();
		
		//check Q table
		if(Q_Table.containsKey(state))
		{
			Q_Table.put(state, new Hashtable<String, Float>());
		}
		
		if(prevState!="")
		{
			float stateReward = CalculateReward();
			if(stateReward!=0)
			{
				setReward(prevState, prevAction, stateReward);
			}
		}
		
		String action = PickActionFrom(Q_Table.get(state).keys());

		if(!Q_Table.get(state).containsKey(action))
		{
			Q_Table.get(state).put(action, init_q_value);
		}
		
		//propogate back
		if(prevState!="")
		{
			float q = 0;
			if(Q_Table.containsKey(prevState) && 
					Q_Table.get(prevState).containsKey(prevAction))
			{
				q = Q_Table.get(prevState).get(prevAction);
			}
			float r = 0;
			if(R_Table.containsKey(prevState) &&
					R_Table.get(prevState).contains(prevAction))
			{
				r = R_Table.get(prevState).get(prevAction);
			}
			
			float maxQ = getMaxQFrom(Q_Table.get(prevState));
			
			float value  = q + alpha * (r + gamma * maxQ - q);
			
			if (value!=0)
			{
				Q_Table.get(prevState).put(prevAction, value);
			}
		}
		
		
		prevHP = cc.getMyHP();
		prevEnemyHP = cc.getEnemyHP();
		prevAction = action;
		prevState = state;
		cc.commandCall(action);
	}
	
	void setReward(String S, String A, float reward )
	{
		if(!R_Table.containsKey(S))
		{
			R_Table.put(S, new Hashtable<String, Float>());
		}
		if(!R_Table.get(S).containsKey(A))
		{
			R_Table.get(S).put(A, reward);
		}
		else
		{
			R_Table.get(S).put(A, reward);
		}
	}
	
	String PickActionFrom(Enumeration<String> enumeration)
	{
		String msg = "";
		
		return msg;
	}
	
	Action getBestActionFrom(Enumeration<String> enumeration)
	{
		
		return null;
	}
	
	float getMaxQFrom(Hashtable<String, Float> hashtable)
	{
		
		return 0;
	}
	
	float CalculateReward()
	{
		int hpDiff = prevHP - cc.getMyHP();
		int enemyHPDiff = prevEnemyHP - cc.getEnemyHP();
		
		int downed = 0;
		int enemyDowned = 0;
		
		String myState = cc.getMyCharacter().getState().toString();
		String oppState = cc.getEnemyCharacter().getState().toString();
		
		if(myState == "DOWN")
		{
			downed = downed_self_reward;
		}
		if(oppState == "DOWN")
		{
			enemyDowned = downed_enemy_reward;
		}
		
		float reward = -hpDiff + enemyHPDiff + downed + enemyDowned;
		return reward;
	}
	
	String encodeState()
	{
		String msg = "";
		
		int energy = cc.getMyEnergy() / energy_grouping;
		float dist_x = Math.abs(
				(cc.getEnemyX() - cc.getMyX())
				/dist_x_grouping);
		float dist_y = (cc.getEnemyY() - cc.getMyY())/dist_y_grouping;
		
		String me = cc.getMyCharacter().toString();
		String status = cc.getMyCharacter().getState().toString();
		String enemyStatus = cc.getEnemyCharacter().getState().toString();
		String oppAction = cc.getEnemyCharacter().getAction().toString();
		
		msg += energy + dist_x + dist_y + status + enemyStatus + oppAction;
		
		return msg;
	}
	
	void WriteQTable(String fname)
	{
		
	}
	
	void WriteRTable(String fname)
	{
		
	}

}
