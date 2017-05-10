import processors.ActionDecider;
import structs.FrameData;
import structs.GameData;
import structs.Key;
import gameInterface.AIInterface;
import commandcenter.CommandCenter;
import data.GlobalConstants;
import filter.ActionFilter;
import simulator.Simulator;
import java.util.Deque;
import java.util.LinkedList;
import enumerate.Action;
import fighting.Attack;

public class Tomatensimulator implements AIInterface {

	private boolean playerNumber;
	private String myCharacter = "unknown";
	
	private ActionDecider decider;
	private String nextCommand = "STAND";
	private FrameData frameData;
	
	private GameData game;
	private structs.Key inKey;
	private CommandCenter cc;
	private Simulator sim;
	
	private String currentCommand = "";
	

	public void close()
	{
	}

	public String getCharacter()
	{
		return this.myCharacter;
	}

	public void getInformation(FrameData arg0)
	{
		//game still initializing
		if(arg0.getFrameNumber() == 0)	
			return;
		
		Deque<Action> myQueue = new LinkedList<Action>();
		Deque<Action> opQueue = new LinkedList<Action>();
		myQueue.add(arg0.getMyCharacter(playerNumber).getAction());
		opQueue.add(arg0.getOpponentCharacter(playerNumber).getAction());

		myQueue.add(arg0.getMyCharacter(playerNumber).getAction());
		opQueue.add(arg0.getOpponentCharacter(playerNumber).getAction());

		myQueue.add(arg0.getMyCharacter(playerNumber).getAction());
		opQueue.add(arg0.getOpponentCharacter(playerNumber).getAction());

		myQueue.add(arg0.getMyCharacter(playerNumber).getAction());
		opQueue.add(arg0.getOpponentCharacter(playerNumber).getAction());

		myQueue.add(arg0.getMyCharacter(playerNumber).getAction());
		opQueue.add(arg0.getOpponentCharacter(playerNumber).getAction());

		myQueue.add(arg0.getMyCharacter(playerNumber).getAction());
		opQueue.add(arg0.getOpponentCharacter(playerNumber).getAction());
		

		myQueue.add(arg0.getMyCharacter(playerNumber).getAction());
		opQueue.add(arg0.getOpponentCharacter(playerNumber).getAction());
		
		FrameData simData = sim.simulate(arg0, this.playerNumber, myQueue, opQueue, 15);
		ActionFilter.simMyData = simData.getMyCharacter(playerNumber);
		ActionFilter.simOpData = simData.getOpponentCharacter(playerNumber);
		
		Deque<Attack> attacks = new LinkedList<Attack>();
		for ( Attack a: simData.getAttack()){
			if(this.playerNumber != a.isPlayerNumber())
			{
				attacks.addLast(a);
			}
		}
		
		ActionFilter.simProj = attacks;
		
		
		ActionFilter.updateMotionInformation(arg0, playerNumber);
		this.cc.setFrameData(arg0, playerNumber);
		this.frameData = arg0;
		nextCommand = this.decider.findBestAction(arg0.getMyCharacter(playerNumber), arg0.getOpponentCharacter(playerNumber), arg0);
	}

	public int initialize(GameData arg0, boolean arg1)
	{
		this.game = arg0;
		this.playerNumber = arg1;
		this.myCharacter = arg0.getMyName(this.playerNumber);
		this.frameData = new FrameData();
		this.inKey = new Key();
		this.decider = new ActionDecider(this.game.getMyMotion(arg1), this.game, this.playerNumber);
		this.cc = new CommandCenter();
		this.sim = new Simulator(arg0);
		GlobalConstants.stageMaxX = arg0.getStageXMax();
		GlobalConstants.stageMaxY = arg0.getStageYMax();
		
		
		return 0;
	}

	
	
	int tmp = 0;
	public Key input()
	{
		return inKey;
	}
		
	public void processing()
	{
		if(!frameData.getEmptyFlag() && frameData.getRemainingTime() > 0)
		{
			if(this.cc.getskillFlag())
			{
				Key k = cc.getSkillKey();
				guardWorkaround(k);
			}
			else
			{
				cc.skillCancel();
				cc.commandCall(nextCommand);
				currentCommand = nextCommand;
			}
		}
	}
	
	private void guardWorkaround(Key k)
	{
		if (currentCommand.contains("GUARD") && noneKey(k))
			return;
		else
			this.inKey = k;
				
	}
	
	private boolean noneKey(Key k)
	{
		return !(k.A || k.B || k.C || k.D || k.L || k.R || k.U);
	}
	
}
