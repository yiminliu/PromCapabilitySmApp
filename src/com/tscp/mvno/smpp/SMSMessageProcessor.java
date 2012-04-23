package com.tscp.mvno.smpp;

import ie.omk.smpp.Connection;
import ie.omk.smpp.message.SMPPResponse;
import ie.omk.smpp.message.SubmitSM;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.tscp.mvno.smpp.util.dbUtil.DBConnectionUtil;
import com.tscp.mvno.smpp.util.dbUtil.SPArgs;
import com.tscp.mvno.smpp.util.dbUtil.StoredProc;
import com.tscp.mvno.smpp.util.logging.SMPPLogger;
import com.tscp.mvno.smpp.util.messageUtil.MessageSupport;
import com.tscp.mvno.smpp.util.smppUtil.SMPPConnectionUtil;

public class SMSMessageProcessor {
	public static final int MESSAGE_TYPE_TEST_SMS		= 0;
	public static final int	MESSAGE_TYPE_PMT_MADE		= 1;
	public static final int MESSAGE_TYPE_BAL_ALERT		= 2;
	public static final int MESSAGE_TYPE_TEXT_ALERT 	= 3;
	public static final int MESSAGE_TYPE_DID_ALERT_A	= 44;
	public static final int MESSAGE_TYPE_DID_ALERT_B	= 45;
	public static final int MESSAGE_TYPE_LOSE_MDN		= 5;
	public static final int MESSAGE_TYPE_JUL09_BLAST	= 6;
	public static final int MESSAGE_TYPE_SENDFAILED		= 7;
	public static final int MESSAGE_TYPE_FLEXMSGLIST	= 10;
	public static final int MESSAGE_TYPE_ACTIVATION  	= 100;	
	public static final int MESSAGE_TYPE_PROM_CAPABILITY = 110;
	
	private Connection 		smppConnection;
	private DBConnectionUtil	dbConnection;
	private static SMPPLogger logger = new SMPPLogger();
		
	public SMSMessageProcessor() {
	}

    public static void main(String[] args) { 
    	
    	logger.trace("********** Start SMSMessageProcessor ***********");
    	   
    	List<SubmitSM> msgList = null;
    	
    	int messageType = MESSAGE_TYPE_PROM_CAPABILITY;
    	
    	try {
            
    		SMSMessageProcessor messageProcessor = new SMSMessageProcessor();
    	
    		logger.trace("********** Initialize the connections and logger *********");
    		
    		messageProcessor.init();
    		
    		logger.trace("********** Get message list ***********");
    		
    		msgList = messageProcessor.getMessageList(messageType);
    	    		    	      	
    	    logger.trace("********** Process the messages ********");
    	
    	    messageProcessor.processMessage(msgList);

    	    logger.trace("********** Close Connections **********");
    	    
    	    messageProcessor.releaseConnections();   
    	}
    	catch(Throwable t){
    		logger.error("Exit the process due to an exception occured : " + t.getMessage());
    	    System.exit(1);	
    	}
    	logger.trace("********** Done SMPP MessageProcessor *********");
    	logger.trace("**********************************************");
    	System.exit(0);
    }

	public void init() throws Exception{
		try{
		  smppConnection = SMPPConnectionUtil.makeConnection();
		  dbConnection = new DBConnectionUtil();
		}
		catch(Exception e){
			logger.error("Error occured while initializing connections, due to: " + e.getMessage());
	        throw e;
		}
	}

    private List<SubmitSM> getMessageList(int iMessageType) throws Exception {
		
		List<SubmitSM> messageList = Collections.emptyList();
		
		switch ( iMessageType ) {
		case SMSMessageProcessor.MESSAGE_TYPE_PROM_CAPABILITY:
			messageList = getPromCapabilityMessageList();
			break; 	
		/*case SMSMessageProcessor.MESSAGE_TYPE_ACTIVATION:
			getActivationMessageList(messageList);
			break;
		case SMSMessageProcessor.MESSAGE_TYPE_TEST_SMS:
			getTestMessageList(messageList);
			break;
		case SMSMessageProcessor.MESSAGE_TYPE_BAL_ALERT:
			getBalanceAlertMessageList(messageList);
			break;
		case SMSMessageProcessor.MESSAGE_TYPE_PMT_MADE:
			getPaymentMadeMessageList(messageList);
			break;
		case SMSMessageProcessor.MESSAGE_TYPE_TEXT_ALERT:
			getBalanceTextAlert(messageList);
			break;
		case SMSMessageProcessor.MESSAGE_TYPE_DID_ALERT_A:
			getDIDNotifcationA(messageList);
			break;
		case SMSMessageProcessor.MESSAGE_TYPE_DID_ALERT_B:
			getDIDNotificationB(messageList);
			break;
		case SMSMessageProcessor.MESSAGE_TYPE_LOSE_MDN:
			getLoseMDNMessageList(messageList);
			break;
		case SMSMessageProcessor.MESSAGE_TYPE_JUL09_BLAST:
			getRateChangeBlastList(messageList);
			break;
		case SMSMessageProcessor.MESSAGE_TYPE_SENDFAILED:
			 getFailedSMSRecords(messageList,mdnSocList);
			break;
		case SMSMessageProcessor.MESSAGE_TYPE_FLEXMSGLIST:
			getNewFLEXMessageList(messageList);
			break;
		*/default:
			messageList = null;
		}		
		logger.info("SMS List returned with "+messageList.size()+" elements.");
	
		return messageList;
	}

	public void processMessage(List<SubmitSM> smsList) throws Exception{
				
		String messageId = null;
				
		int messageSentCounter = 0;		
	
		for( int j = 0; j < smsList.size(); j++ ) {
			logger.info("Preparing message for MDN: "+smsList.get(j).getDestination().getAddress()+" with Message: "+smsList.get(j).getMessageText());
			String messageBlockingSOC = "";
			
			try {
				if(!smppConnection.isBound()){ // is the smpp connection is bound, need to bind
				   SMPPConnectionUtil.bind(smppConnection);
			    }
				messageId = sendRequest(smsList.get(j).getDestination().getAddress(),smsList.get(j).getMessageText());
			    logger.info("Message was sent out successfully to: " + smsList.get(j).getDestination().getAddress());
			}
			catch(Exception e){
				logger.error("Exception occured in processMessage(): " + e.getMessage());
				if( j == smsList.size() - 1 ) {
					SMPPConnectionUtil.unbind(smppConnection);
					throw e;
				}	
			}
			
			if( messageId != null && messageId.trim().length() > 0 && messageBlockingSOC != null && messageBlockingSOC.trim().length() > 0 ) {
				logger.info("Attempting to reinstate "+messageBlockingSOC+" on MDN "+smsList.get(j).getDestination().getAddress());
				MessageSupport.modifySoc(smsList.get(j).getDestination().getAddress(), messageBlockingSOC, MessageSupport.ACTION_ADD);
				logger.info("Adding of "+messageBlockingSOC+" completed for MDN "+smsList.get(j).getDestination().getAddress());
			}
		
			if( messageSentCounter == 100 ) {
				SMPPConnectionUtil.unbind(smppConnection);
				messageSentCounter = 0;
			}
			//once we're done traversing the list of pending SMS messages, we want to unbind our connection.
			if( j == smsList.size() - 1 ) {
				SMPPConnectionUtil.unbind(smppConnection);
			}
			++messageSentCounter;			
		}
	}
	
	private String sendRequest(String iPhoneNumber, String iMessage) throws Exception{
		
		logger.info("send request to " + iPhoneNumber);
		String retValue = null;
		try {
//			SMPPRequest smppRequest;
			ie.omk.smpp.message.SubmitSM shortMsg = new SubmitSM();
			ie.omk.smpp.Address destAddress = new ie.omk.smpp.Address();
			destAddress.setAddress(iPhoneNumber);
			
			ie.omk.smpp.Address sendAddress = new ie.omk.smpp.Address();
			sendAddress.setTON(0);
			sendAddress.setNPI(0);
			sendAddress.setAddress(SMPPConnectionUtil.getShortCode());
			shortMsg.setDestination(destAddress);
			shortMsg.setSource(sendAddress);
			shortMsg.setMessageText(iMessage);
			smppConnection.autoAckMessages(true);
			
			logger.info("------ SMPPRequest -------");
			logger.info("SMPPRequest Source Address   = "+shortMsg.getSource().getAddress());
			logger.info("SMPPRequest Message Text     = "+shortMsg.getMessageText());
			logger.info("SMPPRequest Dest Address     = "+shortMsg.getDestination().getAddress());
		
			SMPPResponse smppResponse = smppConnection.sendRequest(shortMsg);
			
//			if( smppResponse.getCommandStatus() == SMPPResponse.)
			if( smppResponse != null ) {
				logger.info("------ SMPPResponse -------");
				logger.info("SMPPResponse Message         = "+smppResponse.getMessage());
				logger.info("SMPPResponse MessageID       = "+smppResponse.getMessageId());
				logger.info("SMPPResponse MessageStatus   = "+smppResponse.getMessageStatus());
				logger.info("SMPPResponse MessageText     = "+smppResponse.getMessageText());
			
				if( smppResponse.getMessageId() == null || smppResponse.getMessageId().trim().length() == 0 ) {
					retValue = smppResponse.getMessageId();
				} else {
					retValue = smppResponse.getMessageId();
				}
			} else {
				logger.warn("SMPPResponse is null!!!");
			}
		} catch( Exception e ) {
			logger.error("!!Error sending request!! due to:  " + e.getMessage());
			throw e;
		}
		return retValue;
	}
	
    private void releaseConnections() {
		
		SMPPConnectionUtil.releaseConnection(smppConnection);
		
		if( dbConnection != null ) {
			try{ 
				dbConnection.releaseConnection();		
			} catch( Exception e ) {
				logger.error("Error closing dbConnection "+e.getMessage());
			} finally {
				dbConnection = null;
			}
		}
	}		
		
	private List<SubmitSM> getPromCapabilityMessageList() throws Exception{ 
		StoredProc sp = new StoredProc(dbConnection.getConnection());
		SPArgs spargs = new SPArgs();
		spargs.put("sp", "tscp_woms_pkg.sp_get_marketing_sms");// at report@mvnot
		ResultSet rs = null;
		List<SubmitSM> messageList = new ArrayList<SubmitSM>(); 
		
		try {
			rs = sp.exec(spargs,1);
			logger.info("----- Elements retirved -----");
			
			while( rs.next() ) {				
				logger.info("EXTERNAL_ID = " + rs.getString("EXTERNAL_ID"));
				logger.info("TEXT_MESSAGE = " + rs.getString("SMS_MSG"));
				
				SubmitSM shortMessage = new SubmitSM();
				ie.omk.smpp.Address address = new ie.omk.smpp.Address();
				address.setAddress(rs.getString("EXTERNAL_ID"));
				shortMessage.setDestination(address);
				shortMessage.setMessageText(rs.getString("SMS_MSG"));
				messageList.add(shortMessage);
			}
		} catch (Exception e ) {
			logger.error("Error encountered when getting message list..."+e.getMessage());
			throw e;
		} finally {
			sp.close(rs);
			dbConnection.releaseConnection();
		}
		
		//for test
		//SubmitSM shortMessage = new SubmitSM();
		//ie.omk.smpp.Address address = new ie.omk.smpp.Address();
		//address.setAddress("2132566431");
		//shortMessage.setDestination(address);
		//shortMessage.setMessageText("SMS_MSG");
		//messageList.add(shortMessage);
		
		return messageList;
	}
	
	private List<SubmitSM> getActivationMessageList( List<SubmitSM> workingSet ) throws Exception{ 
		StoredProc sp = new StoredProc(dbConnection.getConnection());
		SPArgs spargs = new SPArgs();
		spargs.put("sp", "etc_tscp_woms_pkg.sp_new_activations_text_alert");
		ResultSet rs = sp.exec(spargs,1);
		
		try {
								
			while( rs.next() ) {
				logger.info("EXTERNAL_ID = " + rs.getString("EXTERNAL_ID"));
				logger.info("TEXT_MESSAGE = " + rs.getString("TEXT_MESSAGE"));
				
				SubmitSM shortMessage = new SubmitSM();
				ie.omk.smpp.Address address = new ie.omk.smpp.Address();
				address.setAddress(rs.getString("EXTERNAL_ID"));
				shortMessage.setDestination(address);
				shortMessage.setMessageText(rs.getString("TEXT_MESSAGE"));
				workingSet.add(shortMessage);
			}
		} catch ( Exception e ) {
			logger.error("Error encountered when getting message list..."+e.getMessage());
		} finally {
			sp.close(rs);
			dbConnection.releaseConnection();
		}
		return workingSet;
	}
		
	private void getFailedSMSRecords(List<SubmitSM> messageList, HashMap<String,String> mdnSocList) throws Exception{
		String spName = "DT_GET_SMSFAILED";
		StoredProc sp = new StoredProc(dbConnection.getConnection());
		SPArgs spargs = new SPArgs();
		spargs.put("sp",spName);
		ResultSet rs = sp.exec(spargs,1);
		try {
			while( rs.next() ) {
				SubmitSM shortMessage = new SubmitSM();
				ie.omk.smpp.Address address = new ie.omk.smpp.Address();
				address.setAddress(rs.getString("MDN"));
				shortMessage.setDestination(address);
				shortMessage.setMessageText(rs.getString("TEXT"));
				messageList.add(shortMessage);
				mdnSocList.put(rs.getString("MDN"), rs.getString("SOC"));
			}
		} catch( SQLException sql_ex ) {
			logger.warn("getFailedSMSRecords threw SQL Exception "+sql_ex.getMessage(),sql_ex);
		} finally {
			sp.close(rs);
			dbConnection.releaseConnection();
		}
	} 
	
	private List<SubmitSM> getTestMessageList( List<SubmitSM> workingSet ) {
		//Manual Tester used to avoid connecting to the DB and retrieving live data.
		boolean addMoreRecords = true;
		String input = "";
//		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
		int maxTestCases = 3;
		int i = 0;
//		while(MainHelper.addMoreInformation("Add Messages for testing?", br)) {
		while( addMoreRecords ) {
			SubmitSM shortMessage = new SubmitSM();
			ie.omk.smpp.Address address = new ie.omk.smpp.Address();
//			address.setAddress(MainHelper.populateRequiredField("MDN", br, address.getAddress()));
//			shortMessage.setMessageText(MainHelper.populateRequiredField("Text MSG(160 Chrs)", br, shortMessage.getMessageText()));
			switch( i ) {
			case 0:
				address.setAddress("6268487491");  //WOMS Support Handset
				break;
			case 1:
				address.setAddress("9097440888");	//Dan's AT&T Cell Phone 
				break;
			case 2:
				address.setAddress("3232163660");	//Tscp IT test handset ESN 02209279799
				break;
			case 3:
				//address.setAddress("6262722581");  //Paulina's HandSet
				break;
			case 4:
				//address.setAddress("6263672931"); //Janet Uribe's cell phone
				break;
			default:
				address.setAddress("6268487491");  //WOMS Support Handset
			}
//			address.setAddress("6268487491");  //WOMS Support Handset
//			address.setAddress("6262722581");  //Paulina's HandSet
			//address.setAddress("9135114036");
//			address.setAddress("9135034699");
			shortMessage.setDestination(address);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			String timeStamp = sdf.format(new java.util.Date());
			shortMessage.setMessageText("Testing SMS Application at " + timeStamp );
//			shortMessage.setMessageText("Reciba llamadas desde Mexico sin limite. Su familia hace una llamada local en su pais que suena aqui en su telefono. Para mas informacion llame al 877 771-7736 ");
//			shortMessage.setMessageText("Reciba llamadas desde Mexico SIN LIMITE. Su familia hace una llamada local en Mexico que suena aqui en su casa. Para mas informacion llame al 877 771-7736");
//			shortMessage.setMessageText("Telscape MSG: Reciba llamadas desde Mexico SIN LIMITE. Su familia hace una llamada local en Mexico que suena aqui en su casa. Para detalles 877 771-7736");
//			shortMessage.setMessageText("Telscape Free MSG: Receive UNLIMITED calls from Mexico. Your family dials a local number in Mexico that connects to your home phone. For more information, call 877 771-7736");
//			shortMessage.setMessageText("Telscape MSG: Receive UNLIMITED calls from Mexico. Your family dials a local number in Mexico that rings at home in the US. For details, call 877 771-7736");
			workingSet.add(shortMessage);
			++i;
			if( i == maxTestCases) {
				addMoreRecords = false;
			}
		}
		return workingSet;
	}
}
