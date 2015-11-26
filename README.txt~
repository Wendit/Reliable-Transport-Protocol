
Team Members:   Fu Shen     fushen@gatech.edu
                Wendi Tang  wtang45@gatech.edu

Class: CS3251 B
Date: Nov.25th
Assignment: Programming Assignment 2


Compile:
1, javac FAA_server.java; javac FAA_client.java

2, run "emulator" in one terminal: python NetEmu.py 5000

3, run FAA_server in different terminal: java FAA_server 8081 127.0.0.1 5000

4, run FAA_client in different terminal: java FAA_client 8080 127.0.0.1 5000


Commands:
FAA_server Command: Only allow when server is listening

    "window W":     to change max window size to W
    "terminate":    to gracefully shut-down server 

FAA_client Command:
    "connect":      connect to the server
    "get F":        download file F from server
    "post F":       upload file F to server
    "disconnect":   close the connection


File Description:
    FAA_server.java:        the server application
    FAA_client.java:        the client application
    FAA_UI.java:            the base abstract for server and client

    AAPSocket.java:         the socket of FAA protocol
    AAPServerSocket.java:   the server socket for FAA protocol
    AAPPacket.java:         packet structure of FAA protocol including header and data
    AAPInputStream.java:    inputstream for reading data
    AAPOutputStream.java:   outputstream for sending data
    AAPUtils.java:          help functions for AAPPacket
    ByteBufferQueue.java:   data structure for inputstream buffer

    Exception java files:
        ConnectionAbortEarlyException.java, FileTransferException.java, FlagNotFoundException.java, InvalidCommandException.java, PacketCorruptedException.java, PayloadSizeTooLargeException.java, ServerNotRespondingExceptino.java

Folder Description:
    test_files: file for test
    downloads:  where the client get the file and save to
    uploads:    where the client post the file and server save the file to

Updates:


Known bugs and limitations:



