from py4j.java_gateway import get_field
import random

possible_actions = ["AIR","AIR_A","AIR_B","AIR_D_DB_BA","AIR_D_DB_BB","AIR_D_DF_FA","AIR_D_DF_FB","AIR_DA","AIR_DB","AIR_F_D_DFA","AIR_F_D_DFB","AIR_FA","AIR_FB","AIR_GUARD","AIR_GUARD_RECOV","AIR_RECOV","AIR_UA","AIR_UB","BACK_JUMP","BACK_STEP","CHANGE_DOWN","CROUCH","CROUCH_A","CROUCH_B","CROUCH_FA","CROUCH_FB","CROUCH_GUARD","CROUCH_GUARD_RECOV","CROUCH_RECOV","DASH","DOWN","FOR_JUMP","FORWARD_WALK","JUMP","LANDING","NEUTRAL","RISE","STAND","STAND_A","STAND_B","STAND_D_DB_BA","STAND_D_DB_BB","STAND_D_DF_FA","STAND_D_DF_FB","STAND_D_DF_FC","STAND_F_D_DFA","STAND_F_D_DFB","STAND_FA","STAND_FB","STAND_GUARD","STAND_GUARD_RECOV","STAND_RECOV","THROW_A","THROW_B","THROW_HIT","THROW_SUFFER",]
standing_actions = ["STAND","STAND_A","STAND_B","STAND_D_DB_BA","STAND_D_DB_BB","STAND_D_DF_FA","STAND_D_DF_FB","STAND_D_DF_FC","STAND_F_D_DFA","STAND_F_D_DFB","STAND_FA","STAND_FB","STAND_GUARD","STAND_GUARD_RECOV","STAND_RECOV"]

class BruceL33p(object):
    def __init__(self, gateway):
        self.prev_action = "STAND"
        self.gateway = gateway

        self.width = 96 # The width of the display to obtain
        self.height = 64 # The height of the display to obtain
        self.grayscale = True # The display"s color to obtain true for grayscale, false for RGB
        
    def getCharacter(self):
        return "ZEN"
        
    def close(self):
        pass

    def initialize(self, gameData, player):
        self.inputKey = self.gateway.jvm.structs.Key()
        self.frameData = self.gateway.jvm.structs.FrameData()
        self.cc = self.gateway.jvm.commandcenter.CommandCenter()
            
        self.player = player
        self.gameData = gameData
                
        return 0
        
    def getInformation(self, frameData):
        self.frameData = frameData
        self.cc.setFrameData(self.frameData, self.player)
        
    def input(self):
        return self.inputKey

    def encodeState(self):
##        print "beginning of encode state"
        
##        my_char = self.cc.getMyCharacter()
        
        my_hp = self.cc.getMyHP()
        my_energy = self.cc.getMyEnergy()

##        enemy_char = self.cc.getEnemyCharacter()
        enemy_hp = self.cc.getEnemyHP()
        enemy_energy = self.cc.getEnemyEnergy()

##        print "we got here soo..."
        dist_x = self.cc.getEnemyX() - self.cc.getMyX()
        dist_y = self.cc.getEnemyY() - self.cc.getMyY()
        print "bouta be status"
        myChar = self.cc.getMyCharacter()
        print "myChar:\t", myChar
        status = self.cc.getMyCharacter().getState()
        print "status:\t",status                
        data = (my_hp, my_energy, enemy_hp, enemy_energy, dist_x, dist_y, status, self.prev_action)

        
        return_msg = ""
        for info in data:
            return_msg += str(info) + " "
        return return_msg[:-1]
        
    def processing(self):
        if self.frameData.getEmptyFlag() or self.frameData.getRemainingTime() <= 0:
            self.isGameJustStarted = True
            return
        if self.cc.getskillFlag():
            self.inputKey = self.cc.getSkillKey()
            return

        
        self.inputKey.empty()
        self.cc.skillCancel()

        # get display pixel data
        #displayBuffer = self.frameData.getDisplayByteBufferAsBytes(self.width, self.height, self.grayscale)

        state = self.encodeState()
        print state


        action = random.choice(possible_actions)
        self.prev_action = action
            
        self.cc.commandCall(action)


                        
    # This part is mandatory
    class Java:
        implements = ["gameInterface.AIInterface"]
