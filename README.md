# Text-Based-Protocol-Trivia-Game
C++/Java project on University's Systems programming course.

# Main Idea
The project is about network communication (Client side in C++, Server side in Java).

From the assignment instructions:

"In this assignment you will implement a text-based game server and client. The communication between the
server and the client(s) will be performed using a simple text based protocol (TBGP), which can potentially
support different games; However, your server will only support a single game - Bluffer.
The implementation of the server will be based on the Reactor and Thread-Per-Client servers taught in class.
The client is multithreaded with one thread for handling the socket and another thread to handle stdin".

The Bluffer Game: 

"The Bluffer game is a type of trivia game, with a twist - the players try to fool each other into choosing absurd
answers. The game host asks a series of questions, for which the players try to provide answers that seem real.
The players are then presented with both the real answer, and the fake answers provided by other players, and
have to choose the real one.
Players are awarded 10 points for choosing the correct answer, and 5 for each player that chose one of their fake
answers".

# Main Features
C++: Boost library Threads, communication using sockets.

Java: protocol based communication, Reactor and Thread-Per-Client servers implementation.
