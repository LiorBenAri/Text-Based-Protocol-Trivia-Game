package protocol;

import java.io.IOException;

import MyHandler.MyHandler;
import serverData.*;


public class TBGPProtocolCallback<String> implements ProtocolCallback<String>{

	private MyHandler handler;
	private Room room;
	private String nick;
	private boolean nickInit;

	public void setHandler(MyHandler handler){
		this.handler = handler;
	}

	
	public void setNick(String nick){
		this.nick = nick;
		nickInit = true;
	}
	
	public void setRoom(Room room){
		this.room = room;
	}
	
	public String getNick(){
		return this.nick;
	}
	
	public Room getRoom(){
		return this.room;
	}
		
	public boolean isNickInit(){
		return nickInit;
	}
	
	public void sendMessage(String msg) throws IOException {
	
		handler.sendMsg(msg);
			
	
	}
	
	public TBGPProtocolCallback(){
		nickInit = false;
		this.room = null;
		
	}


	

}
