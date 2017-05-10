import sys, time, threading
from py4j.java_gateway import JavaGateway, GatewayParameters, CallbackServerParameters, get_field
from BruceL33p import BruceL33p

def start_game(manager, agent, opponent=0):
        GAME_NUM = 1000
        program_start = time.time()
        for i in range(GAME_NUM):
                manager.registerAI("BruceL33p", agent)
                print("Start game", i)
                start = time.time()
                if opponent == 0:
                        game = manager.createGame("LUD", "LUD", "BruceL33p", "Thunder01")
                        manager.runGame(game)
                elif opponent == 1:
                        game = manager.createGame("LUD", "LUD", "BruceL33p", "Ranezi")
                        manager.runGame(game)
                elif opponent == 2:
                        game = manager.createGame("LUD", "LUD", "BruceL33p", "JayBot2016")
                        manager.runGame(game)
                print "Game",i+1,"took ", str(time.time()-start), "secs"
                sys.stdout.flush()

        
        
def close_gateway(gateway):
	gateway.close_callback_server()
	gateway.close()
	
def main_process(manager, gateway, agent, i):
	start_game(manager, agent, i)
	close_gateway(gateway)

def worker(i):
        gateway = JavaGateway(gateway_parameters=GatewayParameters(port=(6000+i)), callback_server_parameters=CallbackServerParameters(port=0))
        python_port = gateway.get_callback_server().get_listening_port()
        gateway.java_gateway_server.resetCallbackClient(gateway.java_gateway_server.getCallbackClient().getAddress(), python_port)
        manager = gateway.entry_point

        Bruce = BruceL33p(gateway)

        main_process(manager, gateway, Bruce, i)

        return 

def main():
        
        
        program_start = time.time()

        threads = []
        for i in range(2):
                t = threading.Thread(target=worker, args=(i,))
                threads.append(t)
                t.start()

        for t in threads:
                t.join()
        
        Bruce = BruceL33p("garbage gateway")
        run_end_time = time.time()
        write_start = time.time()
        print "Writing tables..."
        Bruce.WriteQTable("QTable.txt")
        Bruce.WriteRTable("RTable.txt")
        write_end = time.time()
        time.sleep(3)

        print "The 200 games of the program took " + str(run_end_time - program_start) + " seconds"
        print "It took " + str(write_end - write_start) + " seconds to write out the Q&R tables"

        time.sleep(15)
        return

main()
