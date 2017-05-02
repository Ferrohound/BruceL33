import gameInterface.AIInterface;
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
		return null;
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

	@Override
	public void getInformation(FrameData arg0) {
		// TODO Auto-generated method stub

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
		return null;
	}

	/*
	 * This method processes the data from AI. It is executed in each frame.
	 */
	@Override
	public void processing() {
		// TODO Auto-generated method stub

	}

}
