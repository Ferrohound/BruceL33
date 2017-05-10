package filter;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Vector;

import data.Actions;
import data.GlobalConstants;
import structs.CharacterData;
import structs.FrameData;
import structs.GameData;
import structs.HitArea;
import structs.MotionData;
import enumerate.Action;
import enumerate.State;
import fighting.Attack;
import simulator.Simulator;

/**
 * Discard Actions that would either not hit, or would take more energy than possible. Collect the important parameters of the other (viable) actions.
 */
public class ActionFilter {

	public static final int paramCount = 9;
	
	public static CharacterData simMyData;
	public static CharacterData simOpData;
	public static Deque<Attack> simProj;
	
	public static float oppDist = 0; // 0 close - 1 far away
	public static float proDist = 0; // 1 close - 0 faraway
	
	/**
	 * Contains a List of Actions and their respective optimization parameters.
	 */
	public class ActionList
	{
		///[action][damage,eBalanceSuccess,eBalanceBlock,eBalanceMiss,startup, xComponent, yComponent, taken damage, taken energy]
		private int[][] params;
		private Action[] actions;
		
		///@deprecated, please use other constructor
		public ActionList(LinkedList<Action> actions)
		{
			this.params = parameterVector(actions);
			this.actions = (Action[])actions.toArray(new enumerate.Action[actions.size()]);
		}
		
		/**
		 * Create a new ActionList and collect the important optimization parameters.
		 * 
		 * @param actions The actions that are about to be stored in this object.
		 * @param extraFrames If an action is currently performed, only some other actions are able to cancel it. For those actions that cannot, we add an
		 * approximation of frames that have to be waited. The alternative would be to discard these actions completely, but sometimes it could be better to 
		 * wait for a mightier attack than performing a lesser attack on the spot.
		 */
		public ActionList(LinkedList<Action> actions, LinkedList<Integer> extraFrames)
		{
			this.params = parameterVector(actions);
			this.actions = (Action[])actions.toArray(new enumerate.Action[actions.size()]);
			
			for (int i=0; i<actions.size(); i++)
				this.params[0][4] += extraFrames.get(i);
		}
		
		/**
		 * Extract the parameters that are important during the optimization process.
		 * 
		 * @param actions [action][damage,eBalanceSuccess,eBalanceBlock,eBalanceMiss,startup]
		 * @return
		 */
		private int[][] parameterVector(LinkedList<Action> actions)
		{
			int[][] params = new int[actions.size()][paramCount];
			
			for (int i = 0; i<actions.size(); i++)
			{
				MotionData attack = motionData.elementAt(actions.get(i).ordinal());
				params[i][0] = attack.getAttackHitDamage();
				params[i][1] = attack.getAttackHitAddEnergy() + attack.getAttackStartAddEnergy();
				params[i][2] = attack.getAttackGuardAddEnergy() + attack.getAttackStartAddEnergy();
				params[i][3] = attack.getAttackStartAddEnergy();
				params[i][4] = attack.getAttackStartUp();
				params[i][5] = (int) (attack.speedX * oppDist  + attack.speedX * proDist);
				params[i][6] = (int) (-attack.speedY* Math.abs(proDist));
			}
			
			return params;
		}
	
		/**
		 * Get the parameters of the stored actions.
		 * @return
		 */
		public int[][] getParams()
		{
			return params;
		}
		
		/**
		 * Get one of the stored actions.
		 * @param index
		 * @return
		 */
		public Action getAction(int index)
		{
			return actions[index];
		}
	
		private void alterParamsForDmgAndIntake()
		{
			if (this.params.length == 0 && this.actions.length == 0)
				return;
			
			int[][] tmp = this.params.clone();
			this.params = new int[this.actions.length][tmp[0].length + 2];
			for (int i=0; i<tmp.length; i++)
				for (int j=0; j<tmp[i].length; j++)
					this.params[i][j] = tmp[i][j];
		}
		
		private void addDmgAndMPIntake(int index, int dmg, int mp)
		{
			int l = this.params[index].length;
			
			this.params[index][l-2] = dmg;
			this.params[index][l-1] = mp;
		}
	}

	private Vector<MotionData> motionData;
	private int positionOffsetR;
	
	private Simulator sim;
	private boolean player;
	
	/**
	 * The constructor.
	 * @param motion The motionData of our current character. Necessary for evaluating the actions.
	 */
	public ActionFilter(Vector<MotionData> motion, GameData game, boolean player)
	{
		this.motionData = motion;
		HitArea me = motionData.elementAt(Action.valueOf(Actions.actionGround[0].name()).ordinal()).getHit();
		this.positionOffsetR = me.getR();
		
		this.sim = new Simulator(game);
		this.player = player;
	}
	
	
	public static void updateMotionInformation(FrameData fData, boolean playerID){
		
		CharacterData myData = fData.getMyCharacter(playerID);
		CharacterData opData = fData.getOpponentCharacter(playerID);
		Deque<Attack> projectiles = fData.getAttack();
		
		int nFrameDelay = 15; 
		
		int oppDistance = 0;
		if(myData.isFront()){
			
		      oppDistance = opData.getLeft() - myData.getRight() 
					        +  nFrameDelay * Math.abs(myData.getSpeedX() - opData.getSpeedX());
		}
		else{
			oppDistance = myData.getLeft() - opData.getRight() 
			        +  nFrameDelay * Math.abs(myData.getSpeedX() - opData.getSpeedX());
		}
		
		int minProDistance = GlobalConstants.stageMaxX *2;
		float direction = 0;
		//compute distance to closest projctile
		for ( Attack a: projectiles){
			
			//check if dangerous projectile
			if(playerID != a.isPlayerNumber()){
				//System.out.println("a"+ a.isPlayerNumber() +" "+ a.getSpeedX());
				//check if on dangerous height
				HitArea aHit = a.getHitAreaNow();
				if((aHit.getB()+a.getSpeedY()*nFrameDelay) < myData.getTop())
					continue;
				int distance = 0;
				//check if on right side of player
				if(aHit.getL() > myData.getRight()){
					distance = aHit.getL() - myData.getRight()
							 + nFrameDelay * (myData.getSpeedX() + a.getSpeedX());
				}else{
					distance = myData.getLeft() - aHit.getR() 
							 + nFrameDelay * (myData.getSpeedX() + a.getSpeedX());
				}
				
				if(distance<minProDistance){
					minProDistance = distance;
					direction = Math.signum(a.getSpeedX());
				}
				
			}
			
		}		
		
		ActionFilter.oppDist = (float)oppDistance / (float)GlobalConstants.stageMaxX;
		ActionFilter.proDist = (1.0f-(float)minProDistance/ (float)GlobalConstants.stageMaxX) * -1 * direction;
		if (!myData.isFront())
			ActionFilter.proDist *= -1;
	}
	
	public ActionList filterActions(CharacterData me, CharacterData opp, FrameData frame)
	{
		ActionList viableActions = pruneActions(me, opp);
		
		Action oppAction = this.approximateTakenDmg(me, opp);
		viableActions.alterParamsForDmgAndIntake();
		
		for (int i=0; i<viableActions.actions.length; i++)
		{
			int[] tmp = this.calculateDmgAndMPIntake(oppAction, viableActions.getAction(i), frame);
			viableActions.addDmgAndMPIntake(i, tmp[0], tmp[1]);
		}
		
		
		return viableActions;
	}
	
	/**
	 * Filter actions that are impossible in the current state.
	 * 
	 * This includes actions with a higher energy cost, actions in a different state (air or ground)
	 * and attacks out of reach.
	 * 
	 * @param me The CharacterData of our character.
	 * @param opp The CharacterData of our opponent.
	 * @return an ActionList that contains all possible actions and their parameters.
	 */
	public ActionList pruneActions(CharacterData me, CharacterData opp)
	{
		LinkedList<Action> candidates = new LinkedList<Action>();
		LinkedList<Integer> addFrames = new LinkedList<Integer>();
		
		MotionData currentAction = motionData.elementAt(Action.valueOf(me.action.name()).ordinal());
		int maxMotionLevel = calculateMotionLevel(currentAction);
		int currentDuration = Math.max(currentAction.getFrameNumber() - 15 , 0);
		
		Action[] actionPool;
		if (me.getState() == State.AIR)
			actionPool = Actions.actionAir;
		else
			actionPool = Actions.actionGround;
		
		for (int i = 0; i < actionPool.length; i++)
		{
			MotionData motion = motionData.elementAt(Action.valueOf(
					actionPool[i].name()).ordinal());
			
			if (!isPossible(motion, maxMotionLevel, me, opp))
				continue;
			
			candidates.add(actionPool[i]);
			addFrames.add(calculateExtraFrames(motion.motionLevel, maxMotionLevel, currentDuration));
		}
		
		if (me.getState() != State.AIR)
		{
			for (int i = 0; i < Actions.actionMove.length; i++)
			{
				candidates.add(Actions.actionMove[i]);
				addFrames.add(0);
			}
			
			for (int i = 0; i < Actions.actionGuard.length; i++)
			{
				candidates.add(Actions.actionGuard[i]);
				addFrames.add(0);
			}
		}
		
		
		return new ActionList(candidates, addFrames);
    }
	
	/**
	 * Find the motionLevel that could possibly cancel the current action.
	 * 
	 * @param currentAction The MotionData of our last observed Action (could be already finished because of observation delay!).
	 * @return The MotionLevel that could cancel the current Action.
	 */
	private int calculateMotionLevel(MotionData currentAction)
	{		
		if (currentAction.cancelAbleFrame != -1)
			return currentAction.cancelAbleMotionLevel;
		
		return 10;
	}
	
	/**
	 * If an action could not cancel the current one, add the estimated duration until our character is free again to the startup frames.
	 * 
	 * @param motionLevel The actions motionLevel
	 * @param maxLevel The canceable motionLevel
	 * @param currentDuration The estimated number of frames until the character is free again.
	 * @return The value that should be added to the startup frames. (either 0 or currentDuration)
	 */
	private int calculateExtraFrames(int motionLevel, int maxLevel, int currentDuration)
	{
		if (motionLevel > maxLevel)
			return currentDuration;
		else
			return 0;
	}
	
	/**
	 * Check, whether a given attack would be possible and could hit the opponent.
	 * 
	 * @param motion The motionData of the evaluated motion.
	 * @param maxMotionLevel The canceable motionLevel.
	 * @param me Our CharacterData.
	 * @param opp The Opponents CharacterData.
	 * @return True, if the attack is viable.
	 */
	private boolean isPossible(MotionData motion, int maxMotionLevel, CharacterData me, CharacterData opp)
	{
		if (Math.abs(motion.getAttackStartAddEnergy()) > me.getEnergy()) 
			return false;
		
		if (!hits(
				me,opp,
				motion.getAttackHit(),
				motion.getAttackSpeedX(),
				motion.getAttackSpeedY(),
				motion.getAttackActive(),
				motion.getAttackStartUp()))
			return false;
		
		return true;
	}
	
	/**
	 * Check, whether my attack would hit the opponent.
	 * 
	 * @param me Our CharacterData.
	 * @param opp Our Opponents CharacterData.
	 * @param attackArea The static HitArea of the attack. 
	 * @param attSpeedX The horizontal speed of the attacks projectile (0 if no projectile).
	 * @param attSpeedY The vertical speed of the attacks projectile (0 if no projectile).
	 * @param attActive The length of the attacks active time. Important for projectiles.
	 * @param attStartup The length of the attacks startup time.
	 * @return True, if the attack would hit.
	 */
	private boolean hits(CharacterData me, CharacterData opp, HitArea attackArea, int attSpeedX, int attSpeedY, int attActive, int attStartup)
	{
		int[] myPos = {simMyData.getLeft(), simMyData.getRight(), simMyData.y ,simMyData.getTop(), simMyData.getBottom()};
		int[] oppPos = {simOpData.getLeft(), simOpData.getRight(), simMyData.y ,simOpData.getTop(), simOpData.getBottom()};
		int[] xBounds = calculateXBounds(myPos, me.isFront(), attackArea, calculateProjectilePath(attSpeedX, attSpeedY, attActive));
		
		return intersectBoxes(xBounds[0],
				  xBounds[1],
				  myPos[2] + attackArea.getT(),
				  myPos[2] + attackArea.getB(),
				  oppPos[0],
				  oppPos[1],
				  oppPos[3],
				  oppPos[4]);
	}
	
	
	
	private  boolean simProIntersect(){
		boolean collision = false;
		for ( Attack a: ActionFilter.simProj){
			HitArea aH = a.getHitAreaNow();
			if(intersectBoxes(aH.getB(), aH.getR(), aH.getT(), aH.getT(),
					ActionFilter.simMyData.getLeft(), ActionFilter.simMyData.getRight(), ActionFilter.simMyData.getTop(), ActionFilter.simMyData.getBottom())){
				collision = true;
				
			}
			
		}
			
		return collision;
	}
	
	
	/**
	 * Estimate the current position of a character.
	 * 
	 * @param ch The CharacterData of the evaluated character.
	 * @param delay The passed time since the last observation of the character.
	 * @return The estimated position of the character [left, right, y, top, bottom]
	 */
	private int[] movement(CharacterData ch, int delay)
	{
		int[] move = new int[5];
		//x
		if (ch.isFront())
		{
			move[0] = ch.getLeft() + ch.speedX*delay;
			move[1] = ch.getRight() + ch.speedX*delay;
			
			if (move[0] < 0)
			{
				move[1] += Math.abs(move[0]);
				move[0] = 0;
			}
			
		}
		else
		{
			move[0] = ch.getLeft() + ch.speedX*delay;
			move[1] = ch.getRight() + ch.speedX*delay;
			
			if (move[1] > GlobalConstants.stageMaxX)
			{
				move[0] -= Math.abs(GlobalConstants.stageMaxX - move[1]);
				move[1] = GlobalConstants.stageMaxX;
			}
		}
		
		
		//y
		//TODO: flight peak?
		//TODO: + or - ?
		move[2] = ch.y + ch.speedY*delay;
		move[3] = ch.getTop() + ch.speedY*delay;
		move[4] = ch.getBottom() + ch.speedY*delay;
		
		
		return move;
	}
	
	/**
	 * Calculate the horizontal distance that a projectile would passed during its life time.
	 * 
	 * Important: Projectiles with a diagonal movement (vertical Speed != 0) are currently ignored!
	 * 
	 * @param attSpeedX The projectiles horizontal speed.
	 * @param attSpeedY The projectiles vertical speed.
	 * @param duration The projectiles life span.
	 * @return The traversed distance.
	 */
	private int calculateProjectilePath(int attSpeedX, int attSpeedY, int duration)
	{
		if (attSpeedX != 0 && attSpeedY == 0)	//other cases currently not used
		{
			return duration * attSpeedX;
		}
		else
			return 0;
	}
	
	/**
	 * Adapt the attacks hitbox, based on the projectile path.
	 * 
	 * @param me Our characters position vector (as defined in movement() ).
	 * @param isFront True, if our character faces to the right.
	 * @param att The attacks static HitArea.
	 * @param projectileLength The horizontal distance that a projectile could pass.
	 * @return The adapted Hitbox' vertical bounds. [left, right]
	 */
	private int[] calculateXBounds(int[] me, boolean isFront, HitArea att, int projectileLength)
	{
		int[] xBounds = new int[2];	//left,right
		
		//position + projectile
		if (isFront)
		{
			xBounds[0] = me[1] + att.getL() - positionOffsetR;
			xBounds[1] = me[1] + att.getR() - positionOffsetR;
			
			xBounds[1] += projectileLength;
		}
		else
		{
			xBounds[0] = me[0] - att.getR() + positionOffsetR;
			xBounds[1] = me[0] - att.getL() + positionOffsetR;
			
			xBounds[0] -= projectileLength;
		}
		
		return xBounds;
	}
	
	/**
	 * Collision detection of two boxes.
	 * 
	 * @param leftA Hitbox A's left boundary.
	 * @param rightA Hitbox A's right boundary.
	 * @param topA Hitbox A's upper boundary.
	 * @param bottomA Hitbox A's lower boundary.
	 * @param leftB Hitbox B's left boundary.
	 * @param rightB Hitbox B's right boundary.
	 * @param topB Hitbox B's upper boundary.
	 * @param bottomB Hitbox B's lower boundary.
	 * @return True, if the boxes intersect.
	 */
	private boolean intersectBoxes(int leftA, int rightA, int topA, int bottomA, 
			                              int leftB, int rightB, int topB, int bottomB){		
		return ( Math.abs((leftA + rightA)/2 - (leftB + rightB)/2)*2 < Math.abs(rightA + rightB - leftA -leftB)) &&
		         (Math.abs((bottomA + topA)/2 - (bottomB + topB)/2)*2 < Math.abs(bottomA+bottomB - topA - topB));
	}
	
	private int[] calculateDmgAndMPIntake(Action oppAction, Action myAction, FrameData frame)
	{
		Deque<Action> myAct = new LinkedList<Action>();
		myAct.add(myAction);

		Deque<Action> oppAct = new LinkedList<Action>();
		myAct.add(oppAction);
		
		FrameData new_frame = this.sim.simulate(frame, this.player, myAct, oppAct, 30);
		
		int dmg = frame.getMyCharacter(this.player).hp - new_frame.getMyCharacter(this.player).hp;
		int mp = frame.getMyCharacter(this.player).energy - new_frame.getMyCharacter(this.player).energy;
		
		return new int[]{dmg, mp};
	}
	
	/**
	 * Approximate the maximal damage that a character could take this round.
	 * 
	 * @param me Our characters CharacterData.
	 * @param opp Our opponents CharacterData.
	 * @return The maximal damage our character could take.
	 */
	public Action approximateTakenDmg(CharacterData me, CharacterData opp)
	{
		ActionList possibleOppActions = pruneActions(opp, me);
		int[][] params = possibleOppActions.getParams();
		
		if (params.length == 0)
			return Action.STAND;
		
		int maxDMG = params[0][0];
		Action maxAction = Action.STAND;
		for (int i=1; i<params.length; i++)
			if (params[i][0] > maxDMG)
			{
				maxDMG = params[i][0];
				maxAction = possibleOppActions.getAction(i);
			}
		return maxAction;
		//return maxDMG;
	}
	
}
