import structs.FrameData;
import structs.GameData;
import structs.Key;
import gameInterface.AIInterface;

/*
 *  CopyAI performs a motion or an attack previously conducted by its opponent AI. 
 */
public class CopyAI implements AIInterface {
	
	private Key inputKey;
	private boolean player;
	private FrameData frameData;
	
	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getCharacter() {
		// TODO Auto-generated method stub
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
		
		return 0;
	}

	@Override
	public Key input() {
		// TODO Auto-generated method stub
		return inputKey;
	}

	@Override
	public void processing() {
		// TODO Auto-generated method stub
		if(!frameData.getEmptyFlag()){
			if(frameData.getRemainingTime()>0){
				// gets the opponent's Key.
				inputKey = frameData.getKeyData().getOpponentKey(player);
				inputKey.R = inputKey.R ? false : true;
				inputKey.L = inputKey.L ? false : true;
			}
		}
	}

}