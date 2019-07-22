# sr201-control
sr201 command line tool for controlling sr201 board.

This tools does not set up the remote features. It is simply to configure the
device on a local network and switch it on and off.

All responses to the console are in JSON format so a ReST service that 
invokes the tools can read the re-directed output and return it directly to 
a web application.

For simple on/off commands :

