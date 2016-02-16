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

private final int init=0;
private final int success=1;
private final int failure=2;
private Map<String, Integer> tallyTable = new HashMap<String, Integer>(); 
private ArrayList<String> voterTable = new ArrayList<String>();
private String [] candidateIDs = {"14", "5", "10", "2"}; //only temporary until admin submits list

private int state;

public componentMy(){
    state=init;
}

/* just a trivial example */

private void doAuthentication(String first,String last,String passwd){ //should be changed for voting system specifics

if (first.equals("xin")&&last.equals("li")&&passwd.equals("pass"))
    state=success;
else 
    state=failure;
}

//After checking that the voter has not voted before, the candidateID will be verified
//If cID is valid then the vote will be logged in the tallyTable, else failure
private boolean castVoteAuthentication(String cID){
	for (String s : candidateIDs){
		if (s.equals(cID))
			return true;
	}
	return false;
}

//First want to check that the phone number of the voter has not already voted. Only vote per voter
//If it is their first vote then proceed with authentication of cID, and if valid update voter table.
private void voterAuthentication(String phone, String cID){
	if (!voterTable.contains(phone)){
		state = success;
		if(castVoteAuthentication(cID))
			voterTable.add(phone);
		else
			state = failure;
		/*System.out.println("Voter Table:");
		for (int i=0; i<voterTable.size(); i++){
			System.out.println(voterTable.get(i));
		}*/
	}	
	else
		state = failure;
	//System.out.println();
}
/* function in interface ComponentBase */

//Processes the incoming messages with a key value pairing
public KeyValueList processMsg(KeyValueList kvList){
    int MsgID=Integer.parseInt(kvList.getValue("MsgID"));
    if (MsgID == 701){ //cast vote
		voterAuthentication(kvList.getValue("VoterPhoneNo"), kvList.getValue("CandidateID"));
		KeyValueList kvResult = new KeyValueList();
		kvResult.addPair("MsgID", "711");
		kvResult.addPair("Description", "Authentication Result");
		
		switch (state){
			case success: {
				kvResult.addPair("Authentication", "Vote valid");
				break;
			}
			case failure: {
				kvResult.addPair("Authentication", "Vote invalid");
				break;
			}
		}
		return kvResult;
	}
	else{
		doAuthentication(kvList.getValue("FirstName"),kvList.getValue("LastName"),kvList.getValue("passwd"));
		KeyValueList kvResult = new KeyValueList();
		kvResult.addPair("MsgID","1");
		kvResult.addPair("Description","Authentication Result");

	   switch (state) {
	   case success: {
		  kvResult.addPair("Authentication","success");
		  break;
	   }
	  case failure: {
		 kvResult.addPair("Authentication","failure"); 
		 break;
	   }
	   } 
	  return kvResult; 
  }
}

}