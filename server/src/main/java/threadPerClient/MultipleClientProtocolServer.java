package threadPerClient;

import java.io.IOException;
import java.net.ServerSocket;

import javax.xml.ws.handler.Handler;

import MyHandler.MyHandler;
import protocol.ServerProtocolFactory;
import protocol.TBGPProtocolFactory;


class MultipleClientProtocolServer implements Runnable {
	private ServerSocket serverSocket;
	private int listenPort;
	private TBGPProtocolFactory factory;

	
	
	public MultipleClientProtocolServer(int port, TBGPProtocolFactory p)
	{
		serverSocket = null;
		listenPort = port;
		factory = p;
	}
	
	public void run()
	{
		try {
			serverSocket = new ServerSocket(listenPort);
			System.out.println("Listening...");
		}
		catch (IOException e) {
			System.out.println("Cannot listen on port " + listenPort);
		}
		
		while (true)
		{
			try {
			//	ConnectionHandler newConnection = new ConnectionHandler(serverSocket.accept(), factory.create());
				MyHandler newConnection = new ConnectionHandler(serverSocket.accept(), factory.create());
            new Thread((Runnable) newConnection).start();
			}
			catch (IOException e)
			{
				System.out.println("Failed to accept on port " + listenPort);
			}
		}
	}
	

	// Closes the connection
	public void close() throws IOException
	{
		serverSocket.close();
	}
	
	public static void main(String[] args) throws IOException
	{
		
		// Get port
		int port = Integer.decode(args[0]).intValue();
		
		MultipleClientProtocolServer server = new MultipleClientProtocolServer(port, new TBGPProtocolFactory());
		Thread serverThread = new Thread(server);
      serverThread.start();
		try {
			serverThread.join();
		}
		catch (InterruptedException e)
		{
			System.out.println("Server stopped");
		}
		
		
				
	}
}
