import commandcenter.CommandCenter;
import gameInterface.AIInterface;
import structs.FrameData;
import structs.GameData;
import structs.Key;

public class KeepYourDistanceBot implements AIInterface {
	private GameData gameData;
	private boolean playerNumber;
	private Key inputKey;
	private FrameData frameData;
	private CommandCenter commandCenter;
	
	private int minimumRequiredEnergyForFireball = 0;
	
	private final int COMFORT_DISTANCE = 150;
	private final int CORNERED_DISTANCE = 100;
	
	private final int LEFT_CORNER = -120;
	private final int RIGHT_CORNER = 680;
	
	@Override
	public void close() {
		
	}

	@Override
	public String getCharacter() {
		return CHARACTER_ZEN;
	}

	@Override
	public void getInformation(FrameData frameData) {
		this.frameData = frameData;
		commandCenter.setFrameData(this.frameData, playerNumber);
	}

	@Override
	public int initialize(GameData gameData, boolean playerNumber) {
		this.gameData = gameData;
		this.playerNumber = playerNumber;
		inputKey = new Key();
		frameData = new FrameData();
		commandCenter = new CommandCenter();
		
		minimumRequiredEnergyForFireball = getCharacterMinimumRequiredEnergyForFireBall(getMyCharacterName());
		return 0;
	}
	
	/**
	 * Gets the name of the character that is used which can either be ZEN, GARNET, LUD, or KFM.
	 * @return The name of the character being used
	 */
	private String getMyCharacterName() {
		String character = "";
		if(playerNumber == true)
			character = gameData.getPlayerOneCharacterName();
		else
			character = gameData.getPlayerTwoCharacterName();
		
		return character;
	}
	
	/**
	 * Looks which character is being used and gives the minimum energy that is required by that
	 * character to use its fireball attack.
	 * @param character Can either be ZEN, GARNET, LUD, or KFM
	 * @return minimum energy required by the character to use the fireball attack
	 */
	private int getCharacterMinimumRequiredEnergyForFireBall(String character) {
		if (character == "ZEN")
			return 0;
		else
			return 30;
	}

	@Override
	public Key input() {
		return inputKey;
	}

	@Override
	public void processing() {
		// Check if we can process the next action/skill.
		if (!frameData.getEmptyFlag() && frameData.getRemainingTime() > 0) {
			if (commandCenter.getskillFlag())
				inputKey = commandCenter.getSkillKey();
			else {
				inputKey.empty();
				commandCenter.skillCancel();
				
				// Check if we are a good distance away from the opponent.
				if (commandCenter.getDistanceX() >= COMFORT_DISTANCE)
					attack();
				else
					getAway();
			}
		}
	}

	/**
	 * The AI uses the fireball attack when it has enough energy. Otherwise it kicks.
	 */
	private void attack() {
		if (commandCenter.getMyEnergy() >= minimumRequiredEnergyForFireball)
			commandCenter.commandCall("STAND_D_DF_FA");
		else
			commandCenter.commandCall("STAND_FB");
	}
	
	/**
	 * The AI looks if it has reached the left or right corner and if the opponent is closing in
	 * on it. If that's the case, the AI jumps out of the corner.
	 * Otherwise it dashes backwards whenever the opponent gets too close.
	 */
	private void getAway() {
		if ((commandCenter.getMyX() <= LEFT_CORNER || commandCenter.getMyX() >= RIGHT_CORNER)
				&& commandCenter.getDistanceX() < CORNERED_DISTANCE)
			commandCenter.commandCall("FOR_JUMP");
		else
			commandCenter.commandCall("4");
	}
}
