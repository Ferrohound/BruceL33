from py4j.java_gateway import get_field
import random

possible_actions = ["AIR","AIR_A","AIR_B","AIR_D_DB_BA","AIR_D_DB_BB","AIR_D_DF_FA","AIR_D_DF_FB","AIR_DA","AIR_DB","AIR_F_D_DFA","AIR_F_D_DFB","AIR_FA","AIR_FB","AIR_GUARD","AIR_GUARD_RECOV","AIR_RECOV","AIR_UA","AIR_UB","BACK_JUMP","BACK_STEP","CHANGE_DOWN","CROUCH","CROUCH_A","CROUCH_B","CROUCH_FA","CROUCH_FB","CROUCH_GUARD","CROUCH_GUARD_RECOV","CROUCH_RECOV","DASH","DOWN","FOR_JUMP","FORWARD_WALK","JUMP","LANDING","NEUTRAL","RISE","STAND","STAND_A","STAND_B","STAND_D_DB_BA","STAND_D_DB_BB","STAND_D_DF_FA","STAND_D_DF_FB","STAND_D_DF_FC","STAND_F_D_DFA","STAND_F_D_DFB","STAND_FA","STAND_FB","STAND_GUARD","STAND_GUARD_RECOV","STAND_RECOV","THROW_A","THROW_B","THROW_HIT","THROW_SUFFER",]
#standing_actions = ["STAND","STAND_A","STAND_B","STAND_D_DB_BA","STAND_D_DB_BB","STAND_D_DF_FA","STAND_D_DF_FB","STAND_D_DF_FC","STAND_F_D_DFA","STAND_F_D_DFB","STAND_FA","STAND_FB","STAND_GUARD","STAND_GUARD_RECOV","STAND_RECOV"]




class BruceL33p(object):

##    STATIC MEMBER VARIABLES ACROSS ALL CLASSES
    Q_Table = {}
    R_Table = {}
    init_q_value = 0.0
    init_r_value = 0.0
    downed_self_reward = -2
    downed_enemy_reward = 2

    hp_grouping = 20
    energy_grouping = 10
    dist_x_grouping = 3
    dist_y_grouping = 10
    distance_cut_off = 250

    alpha = 0.8
    gamma = 0.2
    epsilon = 0.3
    
    def __init__(self, gateway):
        self.gateway = gateway

        self.width = 96 # The width of the display to obtain
        self.height = 64 # The height of the display to obtain
        self.grayscale = True # The display"s color to obtain true for grayscale, false for RGB
        self.running = False
##        print "__init__()..."
##        print "__init__()..."
##        print "__init__()..."
##        print "__init__()..."


    def getCharacter(self):
        return "ZEN"


##    WRITE OUT THE self.Q_Table TO A TEXT FILE SO I CAN LOOK AT IT
    def WriteQTable(self, fname):
##        print "Time to write the reward table!"
        out = open(fname, 'w')
        for s in self.Q_Table.keys():
##            print "Writing Q out @ state:\t" + s
            non_zero = False
            for a in self.Q_Table[s].keys():
                if self.Q_Table[s][a] != 0:
                    non_zero = True
                    break

            if non_zero:
                out.write(s+'\n')
            
                for a in self.Q_Table[s].keys():
                    if self.Q_Table[s][a] != 0:
                        out.write('\t'+a+'\t-\t'+str(self.Q_Table[s][a])+'\n')
    ##                    print "Writing Q out @ [state][action]:\t[" + s + "][" + a + "]"
        out.close()
        print "Done Writing Q Table."


##    WRITE OUT THE self.R_Table TO A TEXT FILE SO I CAN LOOK AT IT
    def WriteRTable(self, fname):
##        print "Time to write the reward table!"
        out = open(fname, 'w')
        for s in self.R_Table.keys():
##            print "Writing R out @ state:\t" + s
            out.write(s+'\n')
            for a in self.R_Table[s].keys():
                if self.R_Table[s][a] != 0:
                    out.write('\t'+a+'\t-\t'+str(self.R_Table[s][a])+'\n')
        out.close()
        print "Done Writing R Table."


##    CLOSING FUNCTION CALLED BY JAVA
    def close(self):
##        print "Closing..."
##        print "WriteQTable()..."
##        self.WriteQTable("QTable.txt")
##        print "WriteRTable()..."
##        self.WriteRTable("RTable.txt")
##        print "Finished all writing"
        pass
    
    def initialize(self, gameData, player):
        self.inputKey = self.gateway.jvm.structs.Key()
        self.frameData = self.gateway.jvm.structs.FrameData()
        self.cc = self.gateway.jvm.commandcenter.CommandCenter()
            
        self.player = player
        self.gameData = gameData

        # define prev statuses
        self.reset()
        
        
        
        return 0

    def reset(self):
##        if self.running:    ##  DETERMINE IF WE WON BASED ON PREVIOUS HP - PUNISH THE PREV STATE AND PREV ACTION APPROPRIATELY
##            print "Determining if we won..."
##            if self.prev_my_hp > self.prev_enemy_hp:   ##  WON
##                print "\n\n\nWE WON \tRestarting...\n\n\n"
##                self.R_Table[self.prev_state][self.prev_action] = win_reward
##            else:                                   ##  LOST
##                print "\n\n\nWE LOST/TIED \tRestarting...\n\n\n"
##                print self.prev_state,self.prev_action
##                if not self.prev_state in self.R_Table:
##                    print "The issue is the prev state isn't in the self.R_Table!"
##                self.R_Table[self.prev_state][self.prev_action] = lose_reward
##            self.running = False
##        else:
##            print "Restarted but not the first time"
        
        self.prev_action = "STAND"  # player starts standing
        self.prev_state = ""
        self.prev_my_hp = 0#self.cc.getMyHP()
        self.prev_enemy_hp = 0# self.cc.getEnemyHP()
##        print "Finished self.reset()"
        
    def getInformation(self, frameData):
        self.frameData = frameData
        self.cc.setFrameData(self.frameData, self.player)
        
    def input(self):
        return self.inputKey

    def encodeState(self):
##        my_char = self.cc.getMyCharacter()
##        my_hp = self.cc.getMyHP() / self.hp_grouping
        my_energy = self.cc.getMyEnergy() / self.energy_grouping
##        enemy_char = self.cc.getEnemyCharacter()
##        enemy_hp = self.cc.getEnemyHP() / self.hp_grouping
##        enemy_energy = self.cc.getEnemyEnergy() / self.energy_grouping

        dist_x =  abs( ( self.cc.getEnemyX() - self.cc.getMyX() ) / self.dist_x_grouping )
        if dist_x > self.distance_cut_off / self.dist_x_grouping:
            dist_x = str(self.distance_cut_off) + "+"
        dist_y = ( self.cc.getEnemyY() - self.cc.getMyY() ) / self.dist_y_grouping
        myChar = self.cc.getMyCharacter()
        myStatus = self.cc.getMyCharacter().getState()
        oppStatus = self.cc.getEnemyCharacter().getState()
        oppAction = self.cc.getEnemyCharacter().getAction()
        data = (my_energy, dist_x, dist_y, myStatus, oppStatus, oppAction)
        
        return_msg = ""
        for info in data:
            return_msg += str(info) + " "
        return return_msg[:-1]
        
    def processing(self):
        if self.frameData.getEmptyFlag() or self.frameData.getRemainingTime() <= 0:
            self.isGameJustStarted = True
            self.reset()
            return

##        self.running = True
        if self.cc.getskillFlag():
            self.inputKey = self.cc.getSkillKey()
            return

        self.inputKey.empty()
        self.cc.skillCancel()


##        ENCODE THE CURRENT STATE
        state = self.encodeState()
        if not state in self.Q_Table:
            self.Q_Table[state] = {}
##            for a in possible_actions:
##                self.Q_Table[state][a] = self.init_q_value
            
##        print state


##        CALCULATE THE REWARD FOR THIS STATE, AND THEN PROPOGATE THAT BACK FOR THE PREV STATE
        # if it is not the first frame
        if self.prev_state != "":
            # calculate the reward for this state - if took damage, punish; if dealt damage, reward
            state_reward = self.CalculateReward()
            if state_reward != 0:
                self.setReward(self.prev_state, self.prev_action, state_reward)
##        print "Did the reward"

        
##        PICK THE ACTION THE CONTROLLER WILL TAKE
        action = self.PickActionFrom(self.Q_Table[state])
        if not action in self.Q_Table[state]:
##            print "New action, initializing value to", self.init_q_value
            self.Q_Table[state][action] = self.init_q_value
##        print "Selected the action"

        
##        PROPOGATE BACK THE Q-VALUE 
        if self.prev_state != "":    # if it is not the first frame
            # value = q + self.alpha * (r + self.gamma * maxQ - q)
            q = 0
            if self.prev_state in self.Q_Table and self.prev_action in self.Q_Table[self.prev_state]:
                self.Q_Table[self.prev_state][self.prev_action]
            r = 0
            if self.prev_state in self.R_Table and self.prev_action in self.R_Table[self.prev_state]:
                r = self.R_Table[self.prev_state][self.prev_action]
            maxQ = self.GetMaxQFrom(self.Q_Table[self.prev_state])
            #print "q:\t" + str(q) + "\tr:\t" + str(r) + "\tmaxQ:\t" + str(maxQ)
            value = q + self.alpha * (r + self.gamma * maxQ - q)

            if value != 0:
                self.Q_Table[self.prev_state][self.prev_action] = value
##                print "We set the self.Q_Table value:\t" + str(value)

        self.prev_my_hp = self.cc.getMyHP()
        self.prev_enemy_hp = self.cc.getEnemyHP()
        #print "Set self.prev_my_hp & self.prev_enemy_hp:  ",self.prev_my_hp, self.prev_enemy_hp
        self.prev_action = action
        self.prev_state = state
        self.cc.commandCall(action)

##        print "Finished processing()"            

    def PickActionFrom(self, Q_s):
        if random.random() <= self.epsilon:
##            print "Choosing random action"
            return random.choice(possible_actions)

        return self.GetBestActionFrom(Q_s)
        
    def GetBestActionFrom(self, Q_s):
        possible_acts = Q_s.keys()
        if len(possible_acts) == 0:
##            print "Getting best action -- returning random anyway (size is 0)"
            return random.choice(possible_actions)
        if len(possible_acts) == 1:
            if Q_s[possible_acts[0]] > 0:
##                print "Getting best action -- but there's only 1 (although, it is positively rewarded!"
                return possible_acts[0]
##            print "Getting best action -- returning random anyway (only state is <= 0)"
            return random.choice(possible_actions)
        best_action = random.choice(possible_acts)
        best_q = Q_s[best_action]
        
        for act in possible_acts:
            if Q_s[act] > best_q:
                best_action = act
                best_q = Q_s[act]
                
        if best_q <= 0:
            unchosen = list(set(possible_actions) - set(Q_s.keys()))
            if len(unchosen) > 0:
##                print "Getting best action -- returning random anyway (but from the set of unchosen actions)"
                return random.choice(unchosen)
##        print "We actually chose a previously rewarded action"
        return best_action 

    def GetMaxQFrom(self,Q_s):
        #print "GetMaxQFrom"
        max_q = Q_s[Q_s.keys()[0]]
        for act in Q_s.keys():
            if Q_s[act] > max_q:
                max_q = Q_s[act]
        return max_q

    def CalculateReward(self):
##        print "Calculating state reward"
        my_hp_diff = self.prev_my_hp - self.cc.getMyHP()
        enemy_hp_diff = self.prev_enemy_hp - self.cc.getEnemyHP()

        my_downed = 0
        enemy_downed = 0
        my_state = str(self.cc.getMyCharacter().getState())
        opp_state = str(self.cc.getEnemyCharacter().getState())
##        print "About to calculate enemy downed and stuff"
        if my_state == "DOWN":
            my_downed = self.downed_self_reward
            
        if opp_state == "DOWN":
            enemy_downed = self.downed_enemy_reward
        
        reward = -my_hp_diff + enemy_hp_diff + my_downed + enemy_downed
        #print "Returning the reward:", reward
        return float(reward)

    def setReward(self, s, a, r):
        #print "State of setReward"
        if not s in self.R_Table:
            self.R_Table[s] = {}
##            for act in possible_actions:
##                self.R_Table[s][act] = self.init_r_value
        #print "Got through the initialization step i did"
        self.R_Table[s][a] = r
        #print "Finished setting the reward"
    
    # This part is mandatory
    class Java:
        implements = ["gameInterface.AIInterface"]


