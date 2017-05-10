package data;
import enumerate.Action;

/**
 * Contains different lists for different action types. (air, ground, move, guard)
 */
public class Actions {
    
	public static final Action [] actionAir =
            new Action[] {Action.AIR_GUARD, Action.AIR_A, Action.AIR_B, Action.AIR_DA, Action.AIR_DB,
                Action.AIR_FA, Action.AIR_FB, Action.AIR_UA, Action.AIR_UB, Action.AIR_D_DF_FA,
                Action.AIR_D_DF_FB, Action.AIR_F_D_DFA, Action.AIR_F_D_DFB, Action.AIR_D_DB_BA,
                Action.AIR_D_DB_BB};
    public static final Action[] actionGround =
            new Action[] {Action.STAND_D_DB_BA, Action.THROW_A, Action.THROW_B, Action.STAND_A, Action.STAND_B,
                Action.CROUCH_A, Action.CROUCH_B, Action.STAND_FA, Action.STAND_FB, Action.CROUCH_FA,
                Action.CROUCH_FB, Action.STAND_D_DF_FA, Action.STAND_D_DF_FB, Action.STAND_F_D_DFA,
                Action.STAND_F_D_DFB, Action.STAND_D_DB_BB, Action.STAND_D_DF_FC};
    public static final Action[] actionMove = 
    		new Action[] {Action.BACK_JUMP, Action.BACK_STEP, Action.DASH, Action.CROUCH, Action.STAND, Action.JUMP,
    			Action.FOR_JUMP, Action.FORWARD_WALK};
    public static final Action[] actionGuard = 
    		new Action[] {Action.AIR_GUARD, Action.STAND_GUARD, Action.CROUCH_GUARD};
    
}
