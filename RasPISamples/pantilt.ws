#!/bin/bash
PI4J_HOME=/home/pi/pi4j/pi4j-distribution/target/distro-contents
CP=./classes
CP=$CP:../I2C/classes
CP=$CP:$PI4J_HOME/lib/pi4j-core.jar
CP=$CP:./lib/json.jar
CP=$CP:./lib/java_websocket.jar
#
sudo java -cp $CP raspisamples.PanTiltWebSocket
