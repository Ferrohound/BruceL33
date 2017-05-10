import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import structs.FrameData;
import structs.GameData;
import structs.Key;
import structs.MotionData;
import structs.CharacterData;
import gameInterface.AIInterface;
import commandcenter.CommandCenter;
import enumerate.Action;
import enumerate.State;
import simulator.Simulator;


public class MrAsh implements AIInterface {
	static boolean DEBUG_MODE=false;
	Key inputKey;
	boolean playerNumber;
	FrameData frameData;
	CommandCenter cc;
	Simulator simulator;
	GameData gd;
	boolean justStarted;
	int energy;
	int distanceX;
	CharacterData mych;
	CharacterData oppch;
	Node rootNode;
	
	Action[] actionAir,actionGround;
	Action spSkill;
	String sp;
	LinkedList<Action> myActions,oppActions;
	Vector<MotionData> myMotion,oppMotion;
	
	public void setMyAction() {
	    myActions.clear();

	    int energy = mych.getEnergy();

	    if (mych.getState() == State.AIR) {
	      for (int i = 0; i < actionAir.length; i++) {
	        if (Math.abs(myMotion.elementAt(Action.valueOf(actionAir[i].name()).ordinal())
	            .getAttackStartAddEnergy()) <= energy) {
	          myActions.add(actionAir[i]);
	        }
	      }
	    } else {
	      if (Math.abs(myMotion.elementAt(Action.valueOf(spSkill.name()).ordinal())
	          .getAttackStartAddEnergy()) <= energy) {
	        myActions.add(spSkill);
	      }

	      for (int i = 0; i < actionGround.length; i++) {
	        if (Math.abs(myMotion.elementAt(Action.valueOf(actionGround[i].name()).ordinal())
	            .getAttackStartAddEnergy()) <= energy) {
	          myActions.add(actionGround[i]);
	        }
	      }
	    }

	  }

	public void setOppAction() {
	    oppActions.clear();

	    if (oppch.getState() == State.AIR) {
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
	
	//MCTS FUN
	void mcts(){
		System.out.println("Mcts");
		setMyAction();
		setOppAction();
		rootNode = new Node(frameData, null, myActions, oppActions, gd,
				playerNumber, cc, oppch.getAction());
        rootNode.createNode();

        Action bestAction = rootNode.mcts();
        if (DEBUG_MODE) {
          rootNode.printNode(rootNode);
        }

        cc.commandCall(bestAction.name());
	}
	
	class Condition{
		private int energy=-1,distance=-1;
		private String eOP="",dOP="",stat="",opAct="",action="";
		private boolean corner=false;
		
		public String go(){
			return action;
		}
		
		public boolean check(int energy,int dist,String stat,String act){
			boolean match=true;
			if(this.energy!=-1){
				switch(this.eOP){
				case ">":
					match = energy>this.energy;
					break;
				case ">=":
					match = energy>=this.energy;
					break;
				case "<":
					match = energy<this.energy;
					break;
				case "<=":
					match = energy<=this.energy;
					break;
				case "=":
					match = energy==this.energy;
				}
				if(!match)return false;
			}
			if(this.distance!=-1){
				switch(this.dOP){
				case ">":
					match = dist>this.distance;
					break;
				case ">=":
					match = dist>=this.distance;
					break;
				case "<":
					match = dist<this.distance;
					break;
				case "<=":
					match = dist<=this.distance;
					break;
				case "=":
					match = dist==this.distance;
				}
				if(!match)return false;
			}
			if(!this.stat.isEmpty()){
				match = this.stat.equals(stat);
				if(!match)return false;
			}
			if(!this.opAct.isEmpty()){
				match = this.opAct.equals(act);
				if(!match)return false;
			}
			if(this.corner){
				if(cc.getMyCharacter().left<=20 || cc.getMyCharacter().right>=gd.getStageXMax()-20){
					match = true;
				}
				else {
					return false;
				}
			}
			return true;
		}
		
		public Condition(String eop,int e,String dop,int d,String st,String opAct,String act,boolean isCorner){
			this.energy=e;
			this.eOP=eop;
			this.dOP=dop;
			this.distance=d;
			this.stat=st;
			this.opAct=opAct;
			this.action=act;
			this.corner = isCorner;
		}
		
		public Condition(String read,String act){
			this.action=act;
			
			for(String statement:read.split(",")){
				if(statement.startsWith("distance")){
					if(!Character.isDigit(statement.charAt(9))){
						this.dOP = statement.substring(8, 10);
						this.distance=Integer.parseInt(statement.substring(10));
					}
					else{
						this.dOP = statement.substring(8, 9);
						this.distance=Integer.parseInt(statement.substring(9));
					}
				}
				else if(statement.startsWith("energy")){
					if(!Character.isDigit(statement.charAt(7))){
						this.eOP = statement.substring(6, 8);
						this.energy=Integer.parseInt(statement.substring(8));
					}
					else{
						this.eOP = statement.substring(6, 7);
						this.energy=Integer.parseInt(statement.substring(7));
					}
				}
				else if(statement.startsWith("state")){
					this.stat=statement.substring(6);
				}
				else if(statement.startsWith("action")){
					this.opAct=statement.substring(7).replace("@", " ");
				}
				else if(statement.startsWith("corner")){
					if(statement.substring(7).equals("true")){
						this.corner=true;
					}
					else{
						this.corner=false;
					}
				}
			}
		}
	}
	
	Condition spCond;
	int spRate;
	Map<String,ArrayList<Condition>> actions= new HashMap<String,ArrayList<Condition>>();
	
	BufferedReader br = null;
	Random rnd;
	
	public int initialize(GameData gd, boolean pn) {
		energy=0;
		distanceX=0;
		
		justStarted=true;
		this.gd = gd;
		this.playerNumber = pn;
		this.inputKey = new Key();
		cc = new CommandCenter();
		frameData = new FrameData();
		rnd=new Random();
		simulator = gd.getSimulator();
		
		actionAir = new Action[] {Action.AIR_GUARD, Action.AIR_A, Action.AIR_B, Action.AIR_DA,
					Action.AIR_DB, Action.AIR_FA, Action.AIR_FB, Action.AIR_UA, Action.AIR_UB,
					Action.AIR_D_DF_FA, Action.AIR_D_DF_FB, Action.AIR_F_D_DFA, Action.AIR_F_D_DFB,
					Action.AIR_D_DB_BA, Action.AIR_D_DB_BB};
	    actionGround = new Action[] {Action.STAND_D_DB_BA, Action.BACK_STEP, Action.FORWARD_WALK,
	    			Action.DASH, Action.JUMP, Action.FOR_JUMP, Action.BACK_JUMP, Action.STAND_A,
		            Action.CROUCH_GUARD, Action.THROW_A, Action.THROW_B, Action.STAND_GUARD,
		            Action.STAND_B, Action.CROUCH_A, Action.STAND_F_D_DFA, Action.STAND_F_D_DFB,
		            Action.STAND_FB, Action.CROUCH_FA, Action.CROUCH_FB, Action.STAND_D_DF_FA,
		            Action.STAND_D_DF_FB, Action.CROUCH_B, Action.STAND_FA, Action.STAND_D_DB_BB};
	    spSkill = Action.STAND_D_DF_FC;
	    myMotion = this.playerNumber ? gd.getPlayerOneMotion() : gd.getPlayerTwoMotion();
	    oppMotion = this.playerNumber ? gd.getPlayerTwoMotion() : gd.getPlayerOneMotion();
	    this.myActions = new LinkedList<Action>();
	    this.oppActions = new LinkedList<Action>();
		
		ArrayList<Condition> tempAL=new ArrayList<Condition>();
		try {
			br = new BufferedReader(
					new FileReader("data/aiData/MrAsh/ASH_"+this.getCharacter()+".txt"));
			String line="",temp[];
			line = br.readLine();
			temp = line.split(" ");
			sp=temp[1].replace("@", " ");//STAND_D_DF_F
			line = br.readLine();
			temp = line.split(" ");
			spCond=new Condition(temp[1],"STAND_D_DF_FC");//sp condition
			spRate = Integer.parseInt(temp[2]);
			line = br.readLine();
			
			String stat="";
			while(line != null)
			{
				if(line.equals("AIR")){
					stat="AIR";
					tempAL=new ArrayList<Condition>();
				}
				else if(line.equals("FLOOR")){
					stat="FLOOR";
					tempAL=new ArrayList<Condition>();
				}
				else if(line.equals("END")){
					actions.put(stat, tempAL);
					stat="";
				}
				else{
					if(!stat.isEmpty()){
						temp = line.split(" ");
						tempAL.add(new Condition(temp[0],temp[1].replace("@", " ")));
					}
				}
				line = br.readLine();
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		return 0;
	}

	public String getCharacter() {
		return gd.getMyName(playerNumber);
	}

	public void getInformation(FrameData fd) {
		this.frameData = fd;
		cc.setFrameData(this.frameData, playerNumber);
	}

	public Key input() {
		return inputKey;
	}
	
	boolean getAct=false;
	public void processing() {
		if(!frameData.getEmptyFlag() && frameData.getRemainingTime() > 0){
			if(!justStarted){
				//delay 15 frames + 2 frames later 
				frameData = simulator.simulate(frameData, this.playerNumber, null, null, 17);
			}
			else{
				justStarted = false;
			}
			
			cc.setFrameData(frameData, playerNumber);
			energy = frameData.getMyCharacter(playerNumber).getEnergy();
			mych = cc.getMyCharacter();
			oppch = cc.getEnemyCharacter();
			distanceX = cc.getDistanceX();
			
			String ac = oppch.action.toString();
			
			if (cc.getskillFlag()) {
				inputKey = cc.getSkillKey();
			}
			else {
				inputKey.empty(); 
				cc.skillCancel();
				getAct=false;
				if ( (mych.hp - oppch.hp) <= 350 && (ac.equals("STAND_D_DF_FC")|| (oppch.energy>=300 && distanceX<200))){
					cc.commandCall(sp);
					System.out.println("Warning projectile !!");
				}
				else if (!mych.state.equals(State.AIR) && !mych.state.equals(State.DOWN)) {
					if(spCond.check(energy, distanceX, oppch.getState().name(),oppch.getAction().toString()) &&
							rnd.nextInt(100)<spRate){
						getAct = true;
						cc.commandCall(spCond.go());
					}
					
					if(!getAct){
						for(Condition cond: actions.get("FLOOR")){
							if( cond.check(energy, distanceX, oppch.getState().name(),oppch.getAction().toString())){
								getAct = true;
								cc.commandCall(cond.go());
								break;
							}
						}
					}
					
					if(!getAct){
						
						mcts();
					}
				}
				else if (mych.state.equals(State.AIR)) {
					for(Condition cond: actions.get("AIR")){
						if(cond.check(energy, distanceX, oppch.getState().name(),oppch.getAction().toString())){
							getAct = true;
							cc.commandCall(cond.go());
							break;
						}
					}
					if(!getAct){
						//System.out.println(cc.getMyCharacter().left+" "+cc.getMyCharacter().right);
						mcts();
					}
				}	
				else{
					//System.out.println(cc.getMyCharacter().left+" "+cc.getMyCharacter().right);
					mcts();
				}
			}
		}
		else justStarted = true;
	}
	
	public void close() {
			
	}
}
