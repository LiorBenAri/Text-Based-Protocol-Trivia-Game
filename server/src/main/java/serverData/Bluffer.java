package serverData;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.text.html.HTMLDocument.Iterator;
import javax.xml.crypto.Data;

import com.google.gson.Gson;

import protocol.ProtocolCallback;
import protocol.TBGPProtocolCallback;

public class Bluffer implements Game{
	
	private int round; //round == 0 is first round
	boolean inProgress;
	private Map<String,TBGPProtocolCallback<String>> nickToCllback;  //map from client's nick to it's callback instance
	private int numOfPlayers;
	private Map<Integer ,String> roundToQuestionMap; //map from round number to question
	private Map<Integer ,String> roundToAnswerMap; //map from round number to Answer
	private String currQuestion;
	private String currAnswer;
	private Map<String, Integer> nickToScore;
	private Map<String, Integer> nickToScoreInCurrentRound;
	private Map<String, String> nickToSuggestedAnswer; //map from nick to suggested answers for current round
	private Map<String, Integer> nickToAnswerAttempt;//map from nick to answer attemp for current round
	private int PlayersMadeTXTRESP;
	private int PlayersMadeSELECTRESP;
	private int numOfRealAnswer;
	private ArrayList<String> suggestedAnswers; //correct order of answers
	private Map<String, Boolean> TXTRESPPreformed;
	private Map<String, Boolean> SELECTRESPPreformed;
	Room room;
	
	public Bluffer(Map<String, TBGPProtocolCallback<String>> nickToCllback,  int numOfPlayers, Room room) throws IOException{
		//---------------------------------read json---------------------------------------------
		
	//	String path = "/users/studs/bsc/2015/oronm/workspace-new/Ass3/src/serverData/BLUFFER.json";
		String path = "BLUFFER.json";
		roundToQuestionMap = new HashMap<Integer ,String>();
		roundToAnswerMap = new HashMap<Integer ,String>();

		BufferedReader bufferedReader;
			bufferedReader = new BufferedReader(new FileReader(path));
			Gson gson = new Gson();
			InputData data = gson.fromJson(bufferedReader, InputData.class);
		
			InputQuestion[] questions = data.getQuestions();
			
			for (int i=0; i<questions.length; i++){
				roundToQuestionMap.put(i, questions[i].getQuestionTXT());
				roundToAnswerMap.put(i, questions[i].getAnswerTXT());
			}
		
		//------------------------------------------------------------------------------------
	
		this.room = room;
		inProgress = true;
		round  = -1;
		this.numOfPlayers = numOfPlayers;
		this.nickToCllback = nickToCllback;
		nickToScore = new HashMap<String, Integer>();
		TXTRESPPreformed = new HashMap<String, Boolean>();
		SELECTRESPPreformed = new HashMap<String, Boolean>();
		//load questions

		nickToScore =  new HashMap<String, Integer>();
		for (Entry<String,TBGPProtocolCallback<String>> entry : nickToCllback.entrySet()) {
			nickToScore.put(entry.getKey(), 0);
		}
		initRound();
	}	
	
	public void TxtResp(String nick, String resp) throws IOException {
		
	if (TXTRESPPreformed.get(nick)){	
		nickToCllback.get(nick).sendMessage("GAMEMSG PLAYER ALREADY SUBMITTED HIS FAKE ANSWER");
	}
	else{
		nickToCllback.get(nick).sendMessage("SYSMSG TXTRESP ACCEPTED");
		PlayersMadeTXTRESP++;
		nickToSuggestedAnswer.put(nick, resp.toLowerCase());
		TXTRESPPreformed.put(nick, true);
		if (PlayersMadeTXTRESP == numOfPlayers){         //sends the choices to all players
			for (Entry<String, String> entry : nickToSuggestedAnswer.entrySet()) {
				suggestedAnswers.add(entry.getValue());
			}
			suggestedAnswers.add(currAnswer);
			Collections.shuffle(suggestedAnswers);
			for (int i=0; i<suggestedAnswers.size();i++){
				if (suggestedAnswers.get(i).equals(currAnswer)){
					numOfRealAnswer = i;
				}
			}
		
			String choices = "";
			for (int i=0; i<suggestedAnswers.size(); i++){
				choices = choices + " " + i + "." + suggestedAnswers.get(i);
			}
			
			for (Entry<String, TBGPProtocolCallback<String>> entry : nickToCllback.entrySet()) {
				entry.getValue().sendMessage("ASKCHOICES " + choices);
			}
		}	
	}
}


	public void SelectResp(String nick, int resp) throws IOException {
	
	if (!TXTRESPPreformed.get(nick)){
		nickToCllback.get(nick).sendMessage("GAMEMSG SELECTRESP : PLAYER NEED TO SUBMIT A FAKE ANSWER FIRST");
	}
	else if (SELECTRESPPreformed.get(nick)){
		nickToCllback.get(nick).sendMessage("GAMEMSG SELECTRESP : PLAYER ALREADY SUBMITTED HIS CHOICE");
	}
	else if(resp < 0 || resp >  numOfPlayers ){
		nickToCllback.get(nick).sendMessage("GAMEMSG SELECTRESP : ILLIGAL CHOICE");
	}
	else{
		nickToCllback.get(nick).sendMessage("SYSMSG SELECTRESP ACCEPTED");
		SELECTRESPPreformed.put(nick, true);
		PlayersMadeSELECTRESP++;
		nickToAnswerAttempt.put(nick, resp);
		
		if (PlayersMadeSELECTRESP == numOfPlayers){
			
			for (Entry<String,TBGPProtocolCallback<String>> entry : nickToCllback.entrySet()) {	
				boolean isCorrect = false;
				//check if answer attempt is correct		
		
				if (nickToAnswerAttempt.get(entry.getKey()) == numOfRealAnswer){
					nickToScoreInCurrentRound.put(entry.getKey(), nickToScoreInCurrentRound.get(entry.getKey()) + 10);
					isCorrect = true;
				}
				
				//check if other players chose current players suggested answer
				int currPlayerSuggestedAnswerNum = -1;
				String currPlayerSuggestedAnswer = nickToSuggestedAnswer.get(entry.getValue().getNick());
				
				for (int i=0; i<suggestedAnswers.size();i++){
					if (currPlayerSuggestedAnswer.equals(suggestedAnswers.get(i))){
						currPlayerSuggestedAnswerNum = i;
					}
				}
				
				for (Entry<String,Integer> entry1 : nickToAnswerAttempt.entrySet()) {	//go over all nicks that are different from current nick
					if (!entry1.getValue().equals(entry.getValue())){
						if (currPlayerSuggestedAnswerNum == nickToAnswerAttempt.get(entry1.getKey())){
							nickToScoreInCurrentRound.put(entry.getKey(), nickToScoreInCurrentRound.get(entry.getKey()) + 5);								
						}
					}		
				}
				//update score board
				int pointCurrRount = nickToScoreInCurrentRound.get(entry.getKey());
				nickToScore.put(entry.getKey(), nickToScore.get(entry.getKey()) + pointCurrRount);		
				//-------------------------
				
				entry.getValue().sendMessage("GAMEMSG The correct answer is: " + currAnswer);
				if (isCorrect){			
					entry.getValue().sendMessage("GAMEMSG correct! +" + pointCurrRount + "pts");
				}
				else{
					entry.getValue().sendMessage("GAMEMSG wrong! +" + pointCurrRount + "pts");	
				}						
			}
			
			//make scoreboard output		
			String scoreOutput = "";
			for (Entry<String, Integer> entry : nickToScore.entrySet()) {	
				scoreOutput = scoreOutput + " " + entry.getKey() + ": " + entry.getValue() + "pts";
			}		
			scoreOutput = "GAMEMSG Summary: " + scoreOutput;		
			//--------------
			
			for (Entry<String,TBGPProtocolCallback<String>> entry : nickToCllback.entrySet()) {	
				entry.getValue().sendMessage(scoreOutput);
			}
			if (round < 2){
				initRound();
			}
			else{
				inProgress = false;
				room.clearGame();
			}
			
		}	
	}
}

	public void initRound() throws IOException{
		round++;	
		numOfRealAnswer = -1;
		currQuestion = roundToQuestionMap.get(round);
		currAnswer = roundToAnswerMap.get(round);
		nickToSuggestedAnswer = new HashMap<String, String>();
		nickToAnswerAttempt = new HashMap<String, Integer>();
		PlayersMadeTXTRESP = 0;
		PlayersMadeSELECTRESP = 0;
		
		for (Entry<String,TBGPProtocolCallback<String>> entry : nickToCllback.entrySet()) {
			TXTRESPPreformed.put(entry.getKey(), false);
			SELECTRESPPreformed.put(entry.getKey(), false);
		}
		
		nickToScoreInCurrentRound =  new HashMap<String, Integer>();
		for (Entry<String,TBGPProtocolCallback<String>> entry : nickToCllback.entrySet()) {
			nickToScoreInCurrentRound.put(entry.getKey(), 0);
		}
		suggestedAnswers = new ArrayList<String>();
		
		//send new question
		for (Entry<String,TBGPProtocolCallback<String>> entry : nickToCllback.entrySet()) {	
			entry.getValue().sendMessage("ASKTXT " + currQuestion);
		}
	}
	
	public boolean isInProgress(){
		return inProgress;
	}

	
}
