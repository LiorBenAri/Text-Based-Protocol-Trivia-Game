package serverData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import protocol.TBGPProtocolCallback;

public class Room {
	
	private String roomName;
	private String gameType;
	private Game game;
	private boolean active;
	private Map<String,TBGPProtocolCallback<String>> callbackList;  //map from client's nick to it's callback instance
	int numOfPlayers;
	
	
	private static Map<String ,Integer> scoreBoard; //mapping from client nick to its current score
	
	public Room(String roomName){
		this.roomName = roomName;
		active = false;
		gameType = null;
		game = null;
		callbackList = new HashMap<String, TBGPProtocolCallback<String>>();
		this.numOfPlayers = 0;
	}
	

	public boolean isActive(){
		return active;
	}
	
	public void setActive(){
		if (active){
			active = false;
		}
		else
		{
			active = true;
		}
	}
	
	public void setGameType(String name, Game game){
		this.game = game;
		gameType = name;
		isActive();
	}
	
	
	public String getRoomName(){
		return roomName;
	}
	
	public Game getGame(){
		return game;
	}
	
	public void removeClient(String nick){
		callbackList.remove(nick);		
		this.numOfPlayers = this.numOfPlayers - 1;
	}
	
	public void addClient(TBGPProtocolCallback<String> callback){
		callbackList.put(callback.getNick(), callback);
		this.numOfPlayers = this.numOfPlayers + 1;
	}
	
	public void sendMsg(String nick, String msg) throws IOException{
		for (Entry<String, TBGPProtocolCallback<String>> entry : callbackList.entrySet()) {
			if (!entry.getKey().equals(nick)){
				TBGPProtocolCallback<String> callback = entry.getValue();
				callback.sendMessage("USRMSG " + nick + ": " + msg);
			}
		}
	}
	
	public void startGame(String gameType) throws IOException{
		if (gameType.equals("BLUFFER")){
			this.gameType = "BLUFFER";
			setActive();
			game = new Bluffer(callbackList, numOfPlayers, this);
		
		}
		else{
			//support for other games should be added here
		}
		

		
	}
	
	public void clearGame(){
		gameType = null;
		game = null;
		setActive();
	}
}
