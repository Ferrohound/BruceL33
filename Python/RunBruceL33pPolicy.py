from py4j.java_gateway import get_field
import random

possible_actions = ["AIR","AIR_A","AIR_B","AIR_D_DB_BA","AIR_D_DB_BB","AIR_D_DF_FA","AIR_D_DF_FB","AIR_DA","AIR_DB","AIR_F_D_DFA","AIR_F_D_DFB","AIR_FA","AIR_FB","AIR_GUARD","AIR_GUARD_RECOV","AIR_RECOV","AIR_UA","AIR_UB","BACK_JUMP","BACK_STEP","CHANGE_DOWN","CROUCH","CROUCH_A","CROUCH_B","CROUCH_FA","CROUCH_FB","CROUCH_GUARD","CROUCH_GUARD_RECOV","CROUCH_RECOV","DASH","DOWN","FOR_JUMP","FORWARD_WALK","JUMP","LANDING","NEUTRAL","RISE","STAND","STAND_A","STAND_B","STAND_D_DB_BA","STAND_D_DB_BB","STAND_D_DF_FA","STAND_D_DF_FB","STAND_D_DF_FC","STAND_F_D_DFA","STAND_F_D_DFB","STAND_FA","STAND_FB","STAND_GUARD","STAND_GUARD_RECOV","STAND_RECOV","THROW_A","THROW_B","THROW_HIT","THROW_SUFFER",]
#standing_actions = ["STAND","STAND_A","STAND_B","STAND_D_DB_BA","STAND_D_DB_BB","STAND_D_DF_FA","STAND_D_DF_FB","STAND_D_DF_FC","STAND_F_D_DFA","STAND_F_D_DFB","STAND_FA","STAND_FB","STAND_GUARD","STAND_GUARD_RECOV","STAND_RECOV"]


Q_Table = {}
R_Table = {}
init_q_value = 0.0
init_r_value = 0.0
downed_self_reward = -15
downed_enemy_reward = 15
win_reward = 100
lose_reward = -100

hp_grouping = 20
energy_grouping = 5

alpha = 0.8
gamma = 0.2
epsilon = 0.3


def LoadQTable():
    lines = open("QTable.txt",'r').read().split('\n')
    for line in lines:
        parts = line.split('\t')
        if len(parts) == 1:
            Q_Table[line] = {}
        else:
##            print parts[1],parts[3]
            Q_Table[parts[1]] = float(parts[3])

def LoadRTable():
    lines = open("RTable.txt",'r').read().split('\n')
    for line in lines:
        parts = line.split('\t')
        if len(parts) == 1:
            R_Table[line] = {}
        else:
##            print parts[1],parts[3]
            R_Table[parts[1]] = float(parts[3])

LoadQTable()
LoadRTable()

class RunBruceL33pPolicy(object):
    def __init__(self, gateway):
        self.gateway = gateway
        self.width = 96 # The width of the display to obtain
        self.height = 64 # The height of the display to obtain
        self.grayscale = True # The display"s color to obtain true for grayscale, false for RGB
        self.running = False

    def getCharacter(self):
        return "ZEN"


##    WRITE OUT THE Q_TABLE TO A TEXT FILE SO I CAN LOOK AT IT
    def WriteQTable(self, fname):
        print "Time to write the reward table!"
        out = open(fname, 'w')
        for s in Q_Table.keys():
            print "Writing Q out @ state:\t" + s
            out.write(s+'\n')
            for a in Q_Table[s].keys():
                print "Writing Q out @ [state][action]:\t[" + s + "][" + a + "]"
                out.write('\t'+a+'\t-\t'+str(Q_Table[s][a])+'\n')
        out.close()
        print "Done Writing Q Table."


##    WRITE OUT THE R_TABLE TO A TEXT FILE SO I CAN LOOK AT IT
    def WriteRTable(self, fname):
        print "Time to write the reward table!"
        out = open(fname, 'w')
        for s in R_Table.keys():
            print "Writing R out @ state:\t" + s
            out.write(s+'\n')
            for a in R_Table[s].keys():
                out.write('\t'+a+'\t-\t'+str(R_Table[s][a])+'\n')
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
##        my_hp = self.cc.getMyHP() / hp_grouping
        my_energy = self.cc.getMyEnergy() / energy_grouping
##        enemy_char = self.cc.getEnemyCharacter()
##        enemy_hp = self.cc.getEnemyHP() / hp_grouping
        enemy_energy = self.cc.getEnemyEnergy() / energy_grouping

        dist_x = self.cc.getEnemyX() - self.cc.getMyX()
        dist_y = self.cc.getEnemyY() - self.cc.getMyY()
        myChar = self.cc.getMyCharacter()
        myStatus = self.cc.getMyCharacter().getState()
        oppStatus = self.cc.getEnemyCharacter().getState()
        oppAction = self.cc.getEnemyCharacter().getAction()
        data = (my_energy, enemy_energy, dist_x, dist_y, myStatus, oppStatus, oppAction, self.prev_action)
        
        return_msg = ""
        for info in data:
            return_msg += str(info) + " "
        return return_msg[:-1]
        
    def processing(self):
        if self.frameData.getEmptyFlag() or self.frameData.getRemainingTime() <= 0:
            self.isGameJustStarted = True
            self.reset()
            return

        self.running = True
        if self.cc.getskillFlag():
            self.inputKey = self.cc.getSkillKey()
            return

        self.inputKey.empty()
        self.cc.skillCancel()


##        ENCODE THE CURRENT STATE
        state = self.encodeState()
        if not state in Q_Table:
            Q_Table[state] = {}
            for a in possible_actions:
                Q_Table[state][a] = init_q_value
        print state


##        CALCULATE THE REWARD FOR THIS STATE, AND THEN PROPOGATE THAT BACK FOR THE PREV STATE
        # if it is not the first frame
        if self.prev_state != "":
            # calculate the reward for this state - if took damage, punish; if dealt damage, reward
            state_reward = self.CalculateReward()
##            if state_reward != 0:
##                print "The reward for this frame is:\t" + str(state_reward)
            self.setReward(self.prev_state, self.prev_action, state_reward)


##        PICK THE ACTION THE CONTROLLER WILL TAKE
        action = self.PickActionFrom(Q_Table[state])
        if not action in Q_Table[state]:
            print " we  have  a  problem "
            Q_Table[state][action] = init_q_value

##        print "We picked our action:", action

####        PROPOGATE BACK THE Q-VALUE 
##        if self.prev_state != "":    # if it is not the first frame
##            # value = q + alpha * (r + gamma * maxQ - q)
##            q = Q_Table[self.prev_state][self.prev_action]
##            r = R_Table[self.prev_state][self.prev_action]
##            maxQ = self.GetMaxQFrom(Q_Table[self.prev_state])
####            print "q:\t" + str(q) + "\tr:\t" + str(r) + "\tmaxQ:\t" + str(maxQ)
##            value = q + alpha * (r + gamma * maxQ - q)
##            print "We set the Q_Table value:\t" + str(value)
##            Q_Table[self.prev_state][self.prev_action] = value

        #print "We set the Q_Table values"


        self.prev_my_hp = self.cc.getMyHP()
        self.prev_enemy_hp = self.cc.getEnemyHP()
        #print "Set self.prev_my_hp & self.prev_enemy_hp:  ",self.prev_my_hp, self.prev_enemy_hp
        self.prev_action = action
        self.prev_state = state
        self.cc.commandCall(action)
           

    def PickActionFrom(self, Q_s):
        
        
        #print "Q_s:",Q_s
        return random.choice(Q_s.keys())

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
            print "I am downed!"
            print "I am downed!"
            print "I am downed!"
            print "I am downed!"
            print "I am downed!"
            print "I am downed!"
            my_downed = downed_self_reward
            
        if opp_state == "DOWN":
            print "Enemy is downed!"
            print "Enemy is downed!"
            print "Enemy is downed!"
            print "Enemy is downed!"
            print "Enemy is downed!"
            print "Enemy is downed!"
            enemy_downed = downed_enemy_reward
        
        reward = -my_hp_diff + enemy_hp_diff + my_downed + enemy_downed
        #print "Returning the reward:", reward
        return float(reward)

    def setReward(self, s, a, r):
        #print "State of setReward"
        if not s in R_Table:
            R_Table[s] = {}
            #print "New state in the reward table, initializing the reward for all actions from the state to r0"
            for act in possible_actions:
                R_Table[s][act] = init_r_value
        #print "Got through the initialization step i did"
        R_Table[s][a] = r
        #print "Finished setting the reward"
    
    # This part is mandatory
    class Java:
        implements = ["gameInterface.AIInterface"]

