package serverData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
//import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Map.Entry;

import protocol.ProtocolCallback;
import protocol.TBGPProtocolCallback;

public class ServerData {
	
	private ArrayList<String> supportedGames;  
	private Map<String ,TBGPProtocolCallback<String>> clientToCallback; //map from client's nick to its callback
	private Map<String, Room> roomList;  //mapping from room name to its instance
	private ArrayList<String> commandSet;


	
	protected static class ServerDataHolder {
		protected static ServerData serverData = new ServerData();
	}

	protected ServerData(){
		supportedGames = new ArrayList<String>();
		supportedGames.add("BLUFFER");
		clientToCallback = new HashMap<String, TBGPProtocolCallback<String>>();
		roomList = new HashMap<String, Room>();
		commandSet = new ArrayList<String>();
		commandSet.add("NICK");
		commandSet.add("JOIN");
		commandSet.add("MSG");
		commandSet.add("LISTGAMES");
		commandSet.add("STARTGAME");
		commandSet.add("TXTRESP");
		commandSet.add("SELECTRESP");
		commandSet.add("QUIT");
	}

	public static ServerData getServerData() {
		return ServerDataHolder.serverData;
	}
	
	public synchronized void addNick(String nick, TBGPProtocolCallback<String> protocolCallback){
		clientToCallback.put(nick, protocolCallback);
	}
	
	public synchronized boolean isRoomExsits(String room){
		return roomList.containsKey(room);
	}
	
	public synchronized boolean isNickExist(String nick){
		for (Entry<String, TBGPProtocolCallback<String>> entry : clientToCallback.entrySet()) {
			if (entry.getKey().equals(nick) && entry.getValue() != null){
				return true;
			}
		}
		return false;
	}
	
	public Room getRoom(String room){  //return room instance by name
		return roomList.get(room);
	}
	
	public synchronized void removeClient(String nick){
		clientToCallback.remove(nick);
		
	}
	public synchronized void removeFromRoom(String currRoom, String nick){
		Room room = roomList.get(currRoom);
		room.removeClient(nick);
		
	}
	
	public synchronized void addClientToRoom(String nick, String room){
		Room roomToAdd = roomList.get(room);
		roomToAdd.addClient(clientToCallback.get(nick));
	}
	
	public synchronized void addRoom(String room){
		roomList.put(room, new Room(room));
	}
	
	public String getSupportedGamed(){
		String ans = "";
		for (int i=0; i<supportedGames.size() ; i++){
			ans = ans + supportedGames.get(i) + " ";
		}		
		return ans;
	}
	
	public synchronized void addGame(String game){
		supportedGames.add(game);
	}
	
	public boolean isGameSupported(String game){
		return supportedGames.contains(game);
	}
	
	public boolean isCommandLegal(String cmd){
		return commandSet.contains(cmd);
	}
	
	
	
	
	
}