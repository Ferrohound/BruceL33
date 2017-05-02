import argparse
import sys
from py4j.java_gateway import JavaGateway
from py4j.java_gateway import GatewayParameters
from py4j.java_gateway import CallbackServerParameters
from py4j.java_gateway import get_field
from BasicBot import BasicBot
from DisplayInfo import DisplayInfo
from SandBag import SandBag


def start_game(n_game):
    for i in range(n_game):       
        p1 = BasicBot(gateway)
        # p2 = SandBag(gateway)
        p2 = DisplayInfo(gateway)
        manager.registerAI(p1.__class__.__name__, p1)
        manager.registerAI(p2.__class__.__name__, p2)
        print("Start game", i)
    
        game = manager.createGame(
                        p1.getCharacter(), p2.getCharacter(), 
                        p1.__class__.__name__, 
                        p2.__class__.__name__)
        manager.runGame(game)
    
        print("After game", i)
        sys.stdout.flush()


def close_gateway():
    gateway.close_callback_server()
    gateway.close()


if __name__ == '__main__':

    parser = argparse.ArgumentParser(description='Basic bot vs. Display info')
    parser.add_argument('--port', default=6500, type=int, help='game server port')
    parser.add_argument('-n', '--number', default=1, type=int, help='number of game')
    args = parser.parse_args()

    gateway = JavaGateway(gateway_parameters=GatewayParameters(port=args.port),
                          callback_server_parameters=CallbackServerParameters(port=0))
    python_port = gateway.get_callback_server().get_listening_port()
    gateway.java_gateway_server.resetCallbackClient(
        gateway.java_gateway_server.getCallbackClient().getAddress(), python_port)
    manager = gateway.entry_point

    start_game(args.number)
    close_gateway()


