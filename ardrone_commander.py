#!/usr/bin/python

# export PYTHONPATH=<libardrone> so Python can find ardrone library
import libardrone
import sys

drone = libardrone.ARDrone()

print "Receiving commands... (q to exit)"

try:
    while True:  # Keep running until Quit or Ctrl+C
        command = sys.stdin.readline().strip().lower()

        if command == 'quit' or command == 'q':
            break
        elif command == 'takeoff':
            drone.takeoff()
            drone.hover
        elif command == 'land' or command == 'c':
            drone.land()
        elif command == 'up' or command == 'o':
            drone.move_up()
        elif command == 'down' or command == 'l':
            drone.move_down()
        elif command == 'left' or command == 's':
            drone.move_left()
        elif command == 'right' or command == 'f':
            drone.move_right()
        elif command == 'forward' or command == 'e':
            drone.move_forward()
        elif command == 'backward' or command == 'd':
            drone.move_backward()
        elif command == 'turn_left' or command == 'a':
            drone.turn_left()
        elif command == 'turn_right' or command == 'g':
            drone.turn_right()
        else:
            drone.hover

        print "Command: ", command

        #print 'in loop'
        #drone.hover

finally:
    print "Bye"
    drone.land()
    drone.halt()
