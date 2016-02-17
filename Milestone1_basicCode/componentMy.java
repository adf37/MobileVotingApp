/*
A Simple Example--Authentication Component.
To Create a Component which works with the InterfaceServer,
the interface ComponentBase is required to be implemented.

interface ComponentBase is described in InterfaceServer.java.

Variables:
Passcode: Integer/String or combo
SecurityLevel: Integer
InputMsgID 1: 701 (CastVote with VoterPhoneNo, CandidateID)
InputMsgID 2: 702 (RequestReport with Passcode, N)
InputMsgID 3: 703 (Initialize TallyTable with passcode, candidateList)
OuputMsgID 1: 711 (Acknowledge vote with attribute status 1=dup, 2=invalid, 3=valid)
OutputMsgID 2: 712 (AcknowledgeRequestReport with RankedReport)
OutputMsgID 3: 26 (AcknowledgeRequestReport with AckMsgID, YesNo, Name)
*/

import java.io.*;
import java.util.*;

public class componentMy implements ComponentBase{

private final int dup=1;
private final int invalid=2;
private final int valid=3;
private Map<String, Integer> tallyTable = new HashMap<String, Integer>(); 
private ArrayList<String> voterTable = new ArrayList<String>();
private String [] candidateList; //only temporary until admin submits list

private int state;

public componentMy(){
    state=invalid;
}

/* just a trivial example 

private void doAuthentication(String first,String last,String passwd){ //should be changed for voting system specifics

if (first.equals("xin")&&last.equals("li")&&passwd.equals("pass"))
    state=success;
else 
    state=failure;
}
*/

//After checking that the voter has not voted before, the candidateID will be verified
//If cID is valid then the vote will be logged in the tallyTable, else failure
private boolean castVoteAuthentication(String cID){
	for (String s : candidateList){
		if (s.equals(cID))
			return true;
	}
	return false;
}

//First want to check that the phone number of the voter has not already voted. Only vote per voter
//If it is their first vote then proceed with authentication of cID, and if valid update voter table.
private void voterAuthentication(String phone, String cID){
	if (!voterTable.contains(phone)){
		if(castVoteAuthentication(cID)){ //if not duplicate vote and cID is valid
			voterTable.add(phone);
			int count = tallyTable.get(cID);
			tallyTable.put(cID, ++count); //increment candidate's votes
			state = valid;
		}
		else{
			state = invalid;
		}
	}	
	else
		state = dup;
}

private void initializeCandidates(String password, String [] list){
	if (password.equals("pass")){
		candidateList = new String[list.length];
		for (int i=0; i<list.length; i++){
			candidateList[i] = list[i];
			tallyTable.put(list[i], 0);
		}
		state = valid;
	}
}

/* function in interface ComponentBase */

//Processes the incoming messages with a key value pairing
public KeyValueList processMsg(KeyValueList kvList){
    int MsgID=Integer.parseInt(kvList.getValue("MsgID"));
	KeyValueList kvResult = new KeyValueList();
    if (MsgID == 701){ //cast vote
		voterAuthentication(kvList.getValue("VoterPhoneNo"), kvList.getValue("CandidateID"));
		kvResult.addPair("MsgID", "711");
		kvResult.addPair("Description", "Authentication Result");
		
		switch (state){
			case valid: {
				kvResult.addPair("Authentication", "Vote valid");
				break;
			}
			case invalid: {
				kvResult.addPair("Authentication", "Vote invalid");
				break;
			}
			case dup: {
				kvResult.addPair("Authentication", "Duplicate vote");
				break;
			}
		}
		
	}
	else if (MsgID == 703){ //processes initialization of tally table
		candidateList = kvList.getValue("CandidateList").split(";");
		initializeCandidates(kvList.getValue("Passcode"), candidateList);
		kvResult.addPair("MsgID", "26");
		kvResult.addPair("Description", "Acknowledgement");
		switch(state){
			case valid: {
				kvResult.addPair("AckMsgID", "23");
				kvResult.addPair("YesNo", "Yes");
				kvResult.addPair("Name", "GUI");
				break;
			}
			case invalid: {
				kvResult.addPair("AckMsgID", "23");
				kvResult.addPair("YesNo", "No");
				break;
			}
		}
		
	}
	
  return kvResult;
}

}