Go here for info on how to run the FightingICE
http://www.ice.ci.ritsumei.ac.jp/~ftgaic/index-2h.html

right click on the project and click "export->Java->Jar file" and place it under
FightingICE's data/ai folder

then run FightingICE

--disable-window -> run without visualization

--py4j -> run from python script

--port [portNumber] -> specify the port number

"--limithp [x] [y]"

================================ RUN INSTRUCTIONS =======================================
To run the program:
1] load the project into Eclipse, navigate to the FightingICE folder
and click Run > Run configurations
2] Select Java Application > Main
3] Move to the arguments tab and apply --py4j and --port [portNumber], making sure portNumber
matches the port in the filename of the python file you wish to run.
4] Once you run the application, the program will wait for a python script to be run.
5] Run the python script you were referencing in part 3
6] Wait for the program to load all of the character data, this may take several seconds

For additional information, see the ReadMe located in /Python/