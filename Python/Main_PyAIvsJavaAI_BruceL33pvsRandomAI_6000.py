import sys
import time
from time import sleep
from py4j.java_gateway import JavaGateway, GatewayParameters, CallbackServerParameters, get_field
from BruceL33p import BruceL33p

def start_game():
        GAME_NUM = 10
        Bruce = BruceL33p(gateway)
        program_start = time.time()
        for i in range(GAME_NUM):
                manager.registerAI("BruceL33p", Bruce)
                print("Start game", i)
                start = time.time()
                game = manager.createGame("LUD", "LUD", "BruceL33p", "RandomAI")
                manager.runGame(game)
                print "Game",i,"took ", str(time.time()-start), "secs"
                sys.stdout.flush()

        run_end_time = time.time()
        write_start = time.time()
        print "Writing tables..."
        Bruce.WriteQTable("QTable.txt")
        Bruce.WriteRTable("RTable.txt")
        write_end = time.time()
        sleep(3)

        print "The 200 games of the program took " + str(run_end_time - program_start) + " seconds"
        print "It took " + str(write_end - write_start) + " seconds to write out the Q&R tables"

        raw_input("Hit enter to close this program")
        
def close_gateway():
	gateway.close_callback_server()
	gateway.close()
	
def main_process():
	start_game()
	close_gateway()

args = sys.argv
argc = len(args)
GAME_NUM = 1
gateway = JavaGateway(gateway_parameters=GatewayParameters(port=6000), callback_server_parameters=CallbackServerParameters(port=0))
python_port = gateway.get_callback_server().get_listening_port()
gateway.java_gateway_server.resetCallbackClient(gateway.java_gateway_server.getCallbackClient().getAddress(), python_port)
manager = gateway.entry_point

main_process()
