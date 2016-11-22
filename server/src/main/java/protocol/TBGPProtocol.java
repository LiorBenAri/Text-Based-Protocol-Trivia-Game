package protocol;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;

import serverData.*;


public class TBGPProtocol implements AsyncServerProtocol<String>{

	ServerData data = ServerData.getServerData();
	boolean shouldClose = false;
	
	public void processMessage(String msg, ProtocolCallback<String>  callback){
		String command = msg;
		String param = "";
		if (msg.contains(" ")){
			 command = msg.substring(0, msg.indexOf(" "));
			 param = msg.substring(msg.indexOf(" ") + 1, msg.length());
		}

		
		
		TBGPProtocolCallback<String> protocolCallback = (TBGPProtocolCallback<String>)callback;
	if (data.isCommandLegal(command)){	
		switch (command){		
			case "NICK": {//param == nick		
				if (protocolCallback.isNickInit()){
					try {
						protocolCallback.sendMessage("SYSMSG NICK REJECTED : NICK IS ALREDY INITIALIZED");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if(data.isNickExist(param)){
					try {
						protocolCallback.sendMessage("SYSMSG NICK REJECTED : NICK IS TAKEN, CHOOSE A DIFFERENT ONE");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else{
					protocolCallback.setNick(param);
					data.addNick(param, protocolCallback);		
					try {
						protocolCallback.sendMessage("SYSMSG NICK ACCEPTED");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
				}
				break;
			}
	
			case "JOIN": {	//param == room name
				
				if (protocolCallback.isNickInit()){		
					if (data.isRoomExsits(param)){  //in case the room already exists
						Room room = data.getRoom(param);
						if (room.isActive()){  //in case room is active
							try {
								protocolCallback.sendMessage("SYSMSG JOIN REJECTED : ROOM IS ALREADY ACTIVE");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						else if (protocolCallback.getRoom() != null && protocolCallback.getRoom().isActive()){ //in case client is in an active room already
							try {
								protocolCallback.sendMessage("SYSMSG JOIN REJECTED : CLIENT IS ALREADY IN AN ACTIVE ROOM");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						else { //add client to new room, if in other room remove him from old room
							if (protocolCallback.getRoom() != null){
								data.removeFromRoom(protocolCallback.getRoom().getRoomName(), protocolCallback.getNick()); //reomve in case client is in an unactive different room
							}
							//add client to new room
							data.addClientToRoom(protocolCallback.getNick(), param);
							Room _room = data.getRoom(param);
							protocolCallback.setRoom(_room);
							try {
								protocolCallback.sendMessage("SYSMSG JOIN ACCEPTED");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}				
						}
					}
					else{  //room doesnt exists, create new room
						if (protocolCallback.getRoom() != null && protocolCallback.getRoom().isActive()){ //in case client is in an active room already
							try {
								protocolCallback.sendMessage("SYSMSG JOIN REJECTED : CLIENT IS ALREADY IN ACTIVE ROOM");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						else{
							data.addRoom(param);
							data.addClientToRoom(protocolCallback.getNick(), param);
							Room _room = data.getRoom(param);
							protocolCallback.setRoom(_room);
							try {
								protocolCallback.sendMessage("SYSMSG JOIN ACCEPTED");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					
					} 
				}
				else{
					try {
						protocolCallback.sendMessage("SYSMSG JOIN REJECTED : NICK HAS NOT BEEN INITIALIZED");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				break;
			}
			
			case "MSG": {  //param == msg to send
				if (!protocolCallback.isNickInit()){
					try {
						protocolCallback.sendMessage("SYSMSG MSG REJECTED : NICK IS NOT INITIALIZED");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if (protocolCallback.getRoom() != null){
					try {
						protocolCallback.sendMessage("SYSMSG MSG ACCEPTED");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Room room = protocolCallback.getRoom();
					try {
						room.sendMsg(protocolCallback.getNick(), param);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else{
					try {
						protocolCallback.sendMessage("SYSMSG MSG REJECTED : CLIENT IS NOT INSIDE A ROOM");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				break;
				
			}
			
			case "LISTGAMES": {  // param is empty
				if (!protocolCallback.isNickInit()){
					try {
						protocolCallback.sendMessage("SYSMSG  LISTGAMES REJECTED : NICK IS NOT INITIALIZED");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
				}
				else{
				try {
					protocolCallback.sendMessage("SYSMSG LISTGAMES ACCEPTED " + data.getSupportedGamed());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				}
				break;
			}
			
			case "STARTGAME": {//param == game name (string)
				if (!protocolCallback.isNickInit()){
					try {
						protocolCallback.sendMessage("SYSMSG STARTGAME REJECTED : NICK IS NOT INITIALIZED");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}				
				else if (protocolCallback.getRoom() == null){
					try {
						protocolCallback.sendMessage("SYSMSG  STARTGAME REJECTED :CLIENT IS NOT IN A ROOM");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}			
				else if (protocolCallback.getRoom().isActive()){
					try {
						protocolCallback.sendMessage("SYSMSG STARTGAME REJECTED : ROOM HAS AN ACTIVE GAME ALREADY");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if (data.isGameSupported(param)){
					try {
						protocolCallback.sendMessage("SYSMSG STARTGAME ACCEPTED");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Room room = protocolCallback.getRoom();
					try {
						room.startGame(param);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
				}
				else{
					try {
						protocolCallback.sendMessage("SYSMSG STARTGAME REJECTED : GAME IS NOT SUPPORTED BY SERVER");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}			
				}
				break;
			}
			
			case "TXTRESP": { // param == TXTRESP
				if (!protocolCallback.isNickInit()){
					try {
						protocolCallback.sendMessage("SYSMSG TXTRESP REJECTED : NICK HAS NOT BEEN INITIALIZED");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
				}
				else if (protocolCallback.getRoom() == null){
					try {
						protocolCallback.sendMessage("SYSMSG  TXTRESP REJECTED : CLIENT NOT INSIDE A ROOM");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
				}
				else if (protocolCallback.getRoom() != null && !protocolCallback.getRoom().isActive()){
					try {
						protocolCallback.sendMessage("SYSMSG  TXTRESP REJECTED : NO ACTIVE GAME IN CURRENT ROOM");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else{  //game is in progress
				
					Game game  = protocolCallback.getRoom().getGame();
					try {
						game.TxtResp(protocolCallback.getNick(), param);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				break;
			}
			case "SELECTRESP": { //param == SELECTRESP
				if (!protocolCallback.isNickInit()){
					try {
						protocolCallback.sendMessage("SYSMSG SELECTRESP REJECTED : NICK HAS NOT BEEN INITIALIZED");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
				}
				else if (protocolCallback.getRoom() == null){
					try {
						protocolCallback.sendMessage("SYSMSG SELECTRESP REJECTED : CLIENT NOT INSIDE A ROOM");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
				}
				else if (protocolCallback.getRoom() != null && !protocolCallback.getRoom().isActive()){
					try {
						protocolCallback.sendMessage("SYSMSG SELECTRESP REJECTED : NO ACTIVE GAME IN CURRENT ROOM");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}				
				}
				else if (!isNumeric(param)){
					try {
						protocolCallback.sendMessage("SYSMSG SELECTRESP REJECTED : PARAMETER IS NOT A NUMBER ");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else{  //game is in progress
				
					Game game  = protocolCallback.getRoom().getGame();
					int intParam = Integer.parseInt(param);
					try {
						game.SelectResp(protocolCallback.getNick(), intParam);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				break;
				
				
			}
			case "QUIT": {
				if (!protocolCallback.isNickInit()){
					try {
						protocolCallback.sendMessage("SYSMSG QUIT REJECTED : NICK HAS NOT BEEN INITIALIZED");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
				}
				else if (protocolCallback.getRoom() != null && protocolCallback.getRoom().isActive()){
					try {
						protocolCallback.sendMessage("SYSMSG QUIT REJECTED : CLIENT IS IN AN ACTIVE ROOM");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else{
					data.removeClient(protocolCallback.getNick());
					if (protocolCallback.getRoom() != null){
						data.removeFromRoom(protocolCallback.getRoom().getRoomName(), protocolCallback.getNick());
					
					}	
					try {
						protocolCallback.sendMessage("SYSMSG QUIT ACCEPTED");
						shouldClose = true;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
	
			}
			break;
			
		}
	}
	else{
		try {
			protocolCallback.sendMessage("SYSMSG " + command + " UNIDENTIFIED : ILLIGAL COMMAND");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	}


	public boolean isEnd(String msg) {
		return shouldClose;
	}

	@Override
	public boolean shouldClose() {
		return shouldClose;
	}

	@Override
	public void connectionTerminated() {
		shouldClose = true;
		
	}

	public static boolean isNumeric(String str)
	{
	  NumberFormat formatter = NumberFormat.getInstance();
	  ParsePosition pos = new ParsePosition(0);
	  formatter.parse(str, pos);
	  return str.length() == pos.getIndex();
	}



	

}
