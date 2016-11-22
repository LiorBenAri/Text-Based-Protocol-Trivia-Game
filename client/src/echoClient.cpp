#include <stdlib.h>
#include <boost/locale.hpp>
#include <boost/thread.hpp>
#include "../include/connectionHandler.h"

/**
 * This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
 */

void SocketHandler(ConnectionHandler* _connectionHandler){
	int len;
	while(1){
		// We can use one of three options to read data from the server:
		// 1. Read a fixed number of characters
		// 2. Read a line (up to the newline character using the getline() buffered reader
		// 3. Read up to the null character
		std::string answer;
		// Get back an answer: by using the expected number of bytes (len bytes + newline delimiter)
		// We could also use: connectionHandler.getline(answer) and then get the answer without the newline char at the end
		if (!_connectionHandler->getLine(answer)) {
		}
		else{
			len=answer.length();
			// A C string must end with a 0 char delimiter.  When we filled the answer buffer from the socket
			// we filled up to the \n char - we must make sure now that a 0 char is also present. So we truncate last character.
			answer.resize(len-1);
			std::cout << answer << std::endl;
			if (answer == "SYSMSG QUIT ACCEPTED") {
				_connectionHandler->close();
				std::cout << "Exiting...\n" << std::endl;
				break;
			}
			else{
				_connectionHandler->setShouldClose(false);
			}
		}
	}
	std::cout << "connection Handler is closed" << std::endl;
}

void InputHandler(ConnectionHandler* _connectionHandler){
	//bool lastMsg = false;
	const short bufsize = 1024;//why creating this all over again?
	char buf[bufsize];
	std::cin.getline(buf, bufsize);//Directing input stream (keyboard) to buf. buf is at size bufsize.
	std::string line(buf);//constructing a string from buf.
	if (!_connectionHandler->sendLine(line)) {//connectionHandler.sendLine(line) appends '\n' to the message.
		std::cout << "Sending failed...\n" << std::endl;
	}

	while (!_connectionHandler->isClosed()){
		while (!_connectionHandler->ShouldClose()){
			std::cin.getline(buf, bufsize);//Directing input stream (keyboard) to buf. buf is at size bufsize.
			std::string line(buf);//constructing a string from buf.
			if (line=="QUIT"){
				_connectionHandler->setShouldClose(true);
				//lastMsg = true;
			}
			if (!_connectionHandler->sendLine(line)) {
				std::cout << "Sending failed...\n" << std::endl;
				//lastMsg = false;
			}
			else{
				// connectionHandler.sendLine(line) appends '\n' to the message.
				/*if (lastMsg){
					break;
				}*/
			}
		}
	}
}

int main (int argc, char *argv[]) {
	if (argc < 3) {
		std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
		return -1;
	}
	std::string host = argv[1];
	short port = atoi(argv[2]);

	ConnectionHandler connectionHandler(host, port);
	if (!connectionHandler.connect()) {
		std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
		return 1;
	}

	boost::thread inputHandler(InputHandler, &connectionHandler);
	boost::thread socketThread(SocketHandler, &connectionHandler);

	socketThread.join();
	inputHandler.join();

	return 0;
}