import java.io.*;
import java.util.*;

import commandcenter.CommandCenter;
import enumerate.Action;
import enumerate.State;
import structs.FrameData;
import structs.GameData;
import structs.Key;
import structs.MotionData;
import gameInterface.AIInterface;

public class Snorkel implements AIInterface {

	boolean p;
	GameData gd;
	Key inputKey;
	FrameData fd;
	CommandCenter cc;
	ArrayList<String> mySkill = new ArrayList<>();
	ArrayList<String> oppSkill = new ArrayList<>();
	private int myScore;
	private int opponentScore;
	File file;
	FileWriter pw;
	BufferedWriter bw;
	double r = 0.98;

	double posible;
	int N, R;

	private String[] allAct;
	private int[] Dmg;
	private int[][] Reward;
	private double[][] ucb;
	private int[] hitG;
	private int[] egyG;
	private long line = 0;

	@Override
	public int initialize(GameData gameData, boolean playerNumber) {
		// TODO Auto-generated method stub
		gd = gameData;
		p = playerNumber;

		inputKey = new Key();
		fd = new FrameData();
		cc = new CommandCenter();
		
		BufferedReader br2 = null;
        try {
            br2 = new BufferedReader(new FileReader("data/aiData/MyLog/Slog.txt"));
            while ((br2.readLine()) != null) {
            	line++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br2 != null) {
                    br2.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        file = new File("data/aiData/MyLog/Slog.txt");
		try {
			pw = new FileWriter(file, true);
			bw = new BufferedWriter(pw);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        
        allAct = new String[] { "AIR_A", "AIR_B", "AIR_DA", "AIR_DB", "AIR_FA", "AIR_FB", "AIR_UA", "AIR_UB",
				"AIR_D_DF_FA", "AIR_F_D_DFA", "AIR_D_DB_BA",
				"STAND_D_DB_BA", "THROW_A", "THROW_B", "STAND_A", "STAND_B", "CROUCH_A", "CROUCH_B", "STAND_FA",
				"STAND_FB", "CROUCH_FA", "CROUCH_FB", "STAND_D_DF_FA", "STAND_D_DF_FB", "STAND_F_D_DFA",
				"STAND_F_D_DFB", "STAND_D_DB_BB" }; //New table take off Infinity

		Dmg = new int[allAct.length];
		hitG = new int[allAct.length];
		egyG = new int[allAct.length];
		for (int i = 0; i < allAct.length; i++) {
			switch (allAct[i]) {
			case "THROW_A":
				Dmg[i] = 10;
				hitG[i] = 50;
				egyG[i] = 5;
				break;
			case "THROW_B":
				Dmg[i] = 20;
				hitG[i] = 50;
				egyG[i] = 20;
				break;
			case "STAND_A":
				Dmg[i] = 8;
				hitG[i] = 50;
				break;
			case "STAND_B":
				Dmg[i] = 12;
				hitG[i] = 106;
				break;
			case "CROUCH_A":
				Dmg[i] = 30;
				hitG[i] = 100;
				break;
			case "CROUCH_B":
				Dmg[i] = 10;
				hitG[i] = 105;
				break;
			case "STAND_FA":
				Dmg[i] = 40;
				hitG[i] = 100;
				break;
			case "STAND_FB":
				Dmg[i] = 60;
				hitG[i] = 150;
				break;
			case "CROUCH_FA":
				Dmg[i] = 50;
				hitG[i] = 100;
				break;
			case "CROUCH_FB":
				Dmg[i] = 150;
				hitG[i] = 320;
				break;
			case "STAND_D_DF_FA":
				Dmg[i] = 50;
				hitG[i] = 650;
				break;
			case "STAND_D_DF_FB":
				Dmg[i] = 50;
				hitG[i] = 220;
				egyG[i] = 30;
				break;
			case "STAND_D_DF_FC":
				Dmg[i] = 300;
				hitG[i] = 300;
				egyG[i] = 300;
				break;
			case "STAND_F_D_DFA":
				Dmg[i] = 50;
				hitG[i] = 180;
				break;
			case "STAND_F_D_DFB":
				Dmg[i] = 70;
				hitG[i] = 310;
				egyG[i] = 50;
				break;
			case "STAND_D_DB_BA":
				Dmg[i] = 70;
				hitG[i] = 230;
				break;
			case "STAND_D_DB_BB":
				Dmg[i] = 70;
				hitG[i] = 320;
				egyG[i] = 50;
				break;
			case "AIR_A":
				Dmg[i] = 10;
				hitG[i] = 50;
				break;
			case "AIR_B":
				Dmg[i] = 30;
				hitG[i] = 106;
				break;
			case "AIR_DA":
				Dmg[i] = 20;
				hitG[i] = 80;
				//hitG[i] = 250;
				egyG[i] = 5;
				break;
			case "AIR_DB":
				Dmg[i] = 30;
				hitG[i] = 100;
				//hitG[i] = 265;
				egyG[i] = 5;
				break;
			case "AIR_FA":
				Dmg[i] = 20;
				hitG[i] = 100;
				//hitG[i] = 230;
				break;
			case "AIR_FB":
				Dmg[i] = 30;
				hitG[i] = 100;
				//hitG[i] = 290;
				break;
			case "AIR_UA":
				Dmg[i] = 20;
				hitG[i] = 100;
				//hitG[i] = 225;
				break;
			case "AIR_UB":
				Dmg[i] = 40;
				hitG[i] = 100;
				//hitG[i] = 300;
				break;
			case "AIR_D_DF_FA":
				Dmg[i] = 60;
				hitG[i] = 208;
				break;
			case "AIR_D_DF_FB":
				Dmg[i] = 100;
				hitG[i] = 230;
				egyG[i] = 50;
				break;
			case "AIR_F_D_DFA":
				Dmg[i] = 150;
				hitG[i] = 225;
				egyG[i] = 10;
				break;
			case "AIR_F_D_DFB":
				Dmg[i] = 50;
				hitG[i] = 100;
				egyG[i] = 40;
				break;
			case "AIR_D_DB_BA":
				Dmg[i] = 40;
				hitG[i] = 100;
				egyG[i] = 10;
				break;
			case "AIR_D_DB_BB":
				Dmg[i] = 100;
				hitG[i] = 295;
				egyG[i] = 50;
				break;
			default:
				Dmg[i] = 0;
				hitG[i] = 0;
				egyG[i] = 0;
				break;
			}
		}
        
		Reward = new int[allAct.length][allAct.length];
		ucb = new double[allAct.length][allAct.length];
		int c = 3;
		for (int k = 0; k < allAct.length; k++) {
			for (int j = 0; j < allAct.length; j++) {
				Reward[k][j] = Dmg[k] - Dmg[j];
				ucb[k][j] = (Reward[k][j] + c) * (Math.sqrt((2 * Math.log(line)) / countWord(allAct[j])));
			}
		}
		return 0;
	}
	public int countWord(String word) {
		int count = 0;
		BufferedReader br = null;
        try {
            String expression = "";
            br = new BufferedReader(new FileReader("data/aiData/MyLog/Slog.txt"));
            while ((expression = br.readLine()) != null) {
                if (expression.equalsIgnoreCase(word))
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
		return count;
	}

	@Override
	public void getInformation(FrameData frameData) {
		// TODO Auto-generated method stub
		fd = frameData;
		cc.setFrameData(fd, p);
		
		if(fd.getRemainingTime()<0 && this.fd.getRemainingTime()>=0){
			System.out.println("P1:" + this.fd.getP1().getHp());
			System.out.println("P2:" + this.fd.getP2().getHp());
			myScore += calculateMyScore(this.fd.getP1().getHp(),this.fd.getP2().getHp(),p);
			opponentScore += calculateOpponentScore(this.fd.getP1().getHp(),this.fd.getP2().getHp(),p);
		}

	}
	
	private int calculateMyScore(int p1Hp,int p2Hp,boolean playerNumber){
		int score = 0;
		
		if(playerNumber){
			if(p2Hp != 0 || p1Hp != 0)
			{
				score = 100 * p2Hp / (p1Hp + p2Hp);
			}
			else{
				score = 500;
			}
		}
		else{
			if(p2Hp != 0 || p1Hp != 0)
			{
				score = 100 * p1Hp / (p1Hp + p2Hp);
			}
			else{
				score = 500;
			}
		}
		return score;
	}
	
	private int calculateOpponentScore(int p1Hp,int p2Hp,boolean playerNumber){
		int score = 0;
		if(playerNumber){
			if(p2Hp != 0 || p1Hp != 0)
			{
				score = 1000 * p1Hp / (p1Hp + p2Hp);
			}
			else{
				score = 500;
			}
		}
		else{
			if(p2Hp != 0 || p1Hp != 0)
			{
				score = 1000 * p2Hp / (p1Hp + p2Hp);
			}
			else{
				score = 500;
			}
		}
		return score;
	}

	@Override
	public void processing() {
		// TODO Auto-generated method stub
		if (!fd.getEmptyFlag() && fd.getRemainingTime() > 0) {

			if (cc.getskillFlag()) {
				inputKey = cc.getSkillKey();
			} else {
				inputKey.empty();
				cc.skillCancel();
				Action oppAct = cc.getEnemyCharacter().getAction();
				Action MyAct = cc.getEnemyCharacter().getAction();
				MotionData oppMotion = new MotionData();
				MotionData MyMotion = new MotionData();
				
				if(fd.getRemainingTime() >= 58000 && cc.getEnemyCharacter().getAction().name() == "STAND_D_DF_FA") {
					cc.commandCall("BACK_JUMP");
				}
				if((cc.getMyX() <= -110 || cc.getMyX() >= 650) && cc.getDistanceX() <= 150) {
					cc.commandCall("FOR_JUMP");
				}
				if(cc.getMyEnergy() >= 300 && cc.getDistanceY()==0) {
					cc.commandCall("STAND_D_DF_FC");
				}
				
				MyMotion = this.p ? gd.getPlayerOneMotion().elementAt(MyAct.ordinal())
						: gd.getPlayerTwoMotion().elementAt(MyAct.ordinal());
				oppMotion = this.p ? gd.getPlayerTwoMotion().elementAt(oppAct.ordinal())
						: gd.getPlayerOneMotion().elementAt(oppAct.ordinal());

				double max = -999.9;
				double max2 = -999.9;
				int adv = 25;
				int adv2 = 25;
				int bestIndex = -1;
				String myMo = MyMotion.getMotionName();
				String opMo = oppMotion.getMotionName();
				
				for (int i = 0; i < allAct.length; i++) {
					if (allAct[i].equals(opMo)) {
						for (int n = 0; n < allAct.length; n++) {
							if (ucb[i][n] > max && cc.getDistanceX() <= hitG[n]) {
								max2 = max;
								max = ucb[i][n];
								adv = n;
							} else if (ucb[i][n] > max2 && ucb[i][n] != max  && cc.getDistanceX() <= hitG[n]) {
								max2 = ucb[i][n];
								adv2 = n;
							}
						}
						if (cc.getDistanceX() <= hitG[adv] && cc.getMyEnergy() >= egyG[adv]) {
							bestIndex = adv;
						} else {
							bestIndex = adv2;
						}
						break;
					}
				}
				if (bestIndex != -1) {
					cc.commandCall(allAct[bestIndex]);
				}
				else if(cc.getDistanceX() <= 106) {
					cc.commandCall("STAND_B");
				}
				else {
if ((cc.getEnemyEnergy() >= 300) && ((cc.getMyHP() - cc.getEnemyHP()) <= 300))
	cc.commandCall("FOR_JUMP _B B B");
else if (!cc.getMyCharacter().getState().equals(State.AIR) && !cc.getMyCharacter().getState().equals(State.DOWN)) { //if not in air
if ((cc.getDistanceX() > 150)) {
	cc.commandCall("FOR_JUMP");
}
else if (cc.getMyEnergy() >= 300)
	cc.commandCall("STAND_D_DF_FC");
else if ((cc.getMyEnergy() > 100) && (cc.getMyEnergy() >= 50))
	cc.commandCall("STAND_D_DB_BB");
else if (cc.getMyCharacter().getState().equals(State.AIR))
cc.commandCall("STAND_F_D_DFA");
else if (cc.getDistanceX() > 100)
	cc.commandCall("6 6 6");
else
	cc.commandCall("B");
}
else if ((cc.getDistanceX() <= 150) && (cc.getMyCharacter().getState().equals(State.AIR) || cc.getMyCharacter().getState().equals(State.DOWN)) 
		&& (((gd.getStageXMax() - cc.getMyX())>=200))
		&& ((cc.getMyX() >=200))) {
if (cc.getMyEnergy() >= 5) 
	cc.commandCall("AIR_DB");
else
	cc.commandCall("B");
}	
else
	cc.commandCall("B");
				}
				mySkill.add(myMo);
				oppSkill.add(opMo);
				System.out.println(cc.getDistanceY());
			}
		}
	}
	@Override
	public Key input() {
		// TODO Auto-generated method stub
		return inputKey;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		try {		
				for (String value : mySkill) {
					for (String action : allAct) {
						if(action.equalsIgnoreCase(value)) {
							bw.write(value + "\n");
						}
					}
				}
			//}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public String getCharacter() {
		return CHARACTER_ZEN;
	}

}
