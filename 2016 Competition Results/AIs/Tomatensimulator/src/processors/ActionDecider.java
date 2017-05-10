package processors;
import java.util.Vector;

import filter.ActionFilter;
import filter.ActionFilter.ActionList;
import structs.CharacterData;
import structs.FrameData;
import structs.GameData;
import structs.MotionData;

public class ActionDecider {
	
	static final int numberOfActions = 3;
	
	private ActionFilter filter;
	private float[] weights;	//TODO define weights
	
	public ActionDecider(Vector<MotionData> motion, GameData game, boolean player)
	{
		this.filter = new ActionFilter(motion, game, player);
		this.weights = new float[ActionFilter.paramCount];
		this.weights[0] = 1.0f/1.0f;
		this.weights[1] = 1.0f/10.0f;
		this.weights[2] = 1.0f/20.0f;
		this.weights[3] = 1.0f/8.0f;
		this.weights[4] = -1.0f/1.0f;
		this.weights[5] = 1.0f/2.0f;
		this.weights[6] = 1.0f/2.0f;
		this.weights[7] = -1.5f;
		this.weights[8] = 1.5f;
	}
	
	/**
	 * Find the best action (or one of the bests) for the current state.
	 * @param me
	 * @param opp
	 * @return a command, usable in CommandCenter
	 */
	public String findBestAction(CharacterData me, CharacterData opp, FrameData frame)
	{
		ActionList pruned = this.filter.filterActions(me, opp, frame);
		//System.out.println("Number of actions after pruning: " + pruned.getParams().length + " !" );
		int[] bestIndices = this.filterAction(pruned.getParams(), this.weights);
		
		return this.choose(bestIndices, pruned);
	}
	
	private String choose(int[] best, ActionList viable)
	{
		//TODO random?
		if(best[0] != -1){
			return viable.getAction(best[0]).name();
			}
		else
			return "STAND_A";
	}
	
	//computes weighted value from action parameters
	private float computeValue(int[] values, float[] weights){
		float val = 0;
		for (int i = 0; i < weights.length; i++){
			val += values[i] * weights[i];
			
		}
		return val;
	}
	
	//returns best actions (plus a random one as last element..)
	//assumes all values have same length, and same length as weights
	private int[] filterAction(int[][] values, float[] weights){
		
		int[] bestIndices = new int[numberOfActions+1];
		float[] bestValues = new float[numberOfActions+1];
		
		//initialize arrays
		for(int i = 0; i < bestIndices.length; i++){
			bestIndices[i] = -1;
			bestValues[i] = -100;
		}
		
		//select best according to weight
		for ( int i = 0; i < values.length; i++){
			
			//replace worst element in array
			bestIndices[numberOfActions] = i;
			bestValues[numberOfActions] = computeValue(values[i],weights);
			
			int ringIndex = 0;
			float ringValue = 0;
			//sort actions by values
			for(int j=numberOfActions; j > 0; j--){
				if(bestValues[j] > bestValues[j-1]){
					ringIndex = bestIndices[j];
					bestIndices[j] = bestIndices[j-1];
					bestIndices[j-1] = ringIndex;
					
					ringValue = bestValues[j];
					bestValues[j] = bestValues[j-1];
					bestValues[j-1] = ringValue;
				}
				
			}
			
		}
		
		return bestIndices;
	}
	
}
