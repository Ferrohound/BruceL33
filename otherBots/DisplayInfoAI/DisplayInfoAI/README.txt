AI name: DisplayInfoAI
Author: Makoto Ishihara

---Abstract---
・This AI decides its next action using display pixel data.
・This AI will be released as a sample AI using display information.

---Algorithm---
0. Launch FightingICE with "--black-bg"

1. Get display information of the specified size and mode (WIDTH * HEIGHT, mode: grayScale)

2. Record the most-right x coordinate of the character on left side 
(e.g. ..2124000....00087732) 
-record coordinate (leftX) of the position "4"

3. Record the most-left x coordinate of the character on right side 
(e.g. ..2124000....00087732)
-record coordinate (rightX) of the position "8"

4. Calculate the distance (distance = |leftX - rightX|) 

5. Decide an action based on the distance

---Known Issues---
・Affected by some effects (e.g. hit effect, Hadouken etc...)
・Affected by pixels between its own feet 
(e.g. ...23120003412...)
-footL: 2312, footR: (3412)