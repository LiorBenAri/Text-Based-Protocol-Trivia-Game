CFLAGS:=-c -Wall -Weffc++ -g
LDFLAGS:=-lboost_system -lboost_locale -lboost_thread

all: EchoClient
	g++ -o bin/echoExample bin/connectionHandler.o bin/echoClient.o $(LDFLAGS) 

EchoClient: bin/connectionHandler.o bin/echoClient.o
	
bin/connectionHandler.o: src/connectionHandler.cpp
	g++ $(CFLAGS) -o bin/connectionHandler.o src/connectionHandler.cpp

bin/echoClient.o: src/echoClient.cpp
	g++ $(CFLAGS) -o bin/echoClient.o src/echoClient.cpp
	
.PHONY: clean
clean:
	rm -f bin/*
