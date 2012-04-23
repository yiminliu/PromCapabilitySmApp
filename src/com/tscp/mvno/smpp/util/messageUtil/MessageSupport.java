package com.tscp.mvno.smpp.util.messageUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.tscp.mvno.api.proxy.API3;
import com.tscp.mvno.api.proxy.API3Service;
import com.tscp.mvno.api.proxy.ApiGeneralResponseHolder;
import com.tscp.mvno.api.proxy.ApiResellerSubInquiryResponseHolder;
import com.tscp.mvno.api.proxy.Sali2;
import com.tscp.mvno.api.proxy.ServiceSub2;

public class MessageSupport {
	
	public static final String ACTION_ADD			= "ADD";
	public static final String ACTION_REMOVE		= "REMOVE";
	
	public MessageSupport() {
	}
	
	public String hasMessageBlocking(String iMDN) {
		String retValue = "";
		try {
			API3Service service = new API3Service();
			API3 port = service.getAPI3Port();
			ApiResellerSubInquiryResponseHolder response = port.apIresellerV2SubInquiry(null, iMDN);
			if( response == null ) {
				//throw/raise some kind of exception here
			} else {
				if( response.getServiceList() != null && response.getServiceList().getValue() != null ) {
					for( int i = 0; i < response.getServiceList().getValue().size(); ++i ) {
						if( response.getServiceList().getValue().get(i).getSvcCode().equals("PRSMTBLK") || response.getServiceList().getValue().get(i).getSvcCode().equals("PRSMOMT") ) {
							System.out.println("Index ["+i+"] :: "+response.getServiceList().getValue().get(i).getSvcCode());
							if( response.getServiceList().getValue().get(i).getSvcExprDt() == null || response.getServiceList().getValue().get(i).getSvcExprDt().trim().length() == 0 ) {
								retValue = response.getServiceList().getValue().get(i).getSvcCode();
								break;
							}
						}
					}
				}
			}
		} catch( Exception gen_ex ) {
			System.out.println("Caught generic exception!!!");
			gen_ex.printStackTrace(System.out);
			System.out.println("Attempting to print cause stack trace...");
			gen_ex.getCause().printStackTrace(System.out);
		}
		return retValue;
	}
	
	public static boolean modifySoc(String iMDN, String iSOC, String iAction) {
		boolean retValue = false;
		try {
			API3Service service = new API3Service();
			API3 port = service.getAPI3Port();
			ServiceSub2 serviceSub2 = new ServiceSub2();
			serviceSub2.setPricePlans(null);
			serviceSub2.setMDN(iMDN);
			Sali2 sali2 = new Sali2();
			sali2.setSvcCode(iSOC);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			if( iAction.equals(ACTION_ADD) ) {
				sali2.setSvcEffDt(sdf.format(new Date()));
				serviceSub2.getServices().add(sali2);
			} else if( iAction.equals(ACTION_REMOVE) ) {
				sali2.setSvcExpDt(sdf.format(new Date()));
				serviceSub2.getOldservices().add(sali2);
			}
			ApiGeneralResponseHolder response = port.apIchangeServicePlans(serviceSub2);
			if( response != null ) {
				System.out.println("change services for "+iMDN+" wishing to "+iAction+" "+iSOC+" has returned with the following Message :: "+response.getStatusMessage()+"::"+response.getResponseMessage()+"::"+response.getApiResponseMessage());
				retValue = true;
			}
		} catch( Exception gen_ex ) {
			gen_ex.printStackTrace(System.out);
		}
		return retValue;
	}
	
	public static void main(String[] args) {
		MessageSupport messageSupport = new MessageSupport();
		String lMDN = "6268487491"; //WOMS Support Handset
		
		//String lMDN = "6262722581";
	    //String lMDN = "9097440888";	//Dan's AT&T Cell Phone 
		//String lMDN = "3232163660";	//Tscp IT test handset ESN 02209279799
		//String lMDN = "6262722581";  //Paulina's HandSet
		//String lMDN = "6263672931"; //Janet Uribe's cell phone
		//String lMDN = "9135114036";
		//String lMDN = "9135034699";
	
		System.out.println("Testing to see if "+lMDN+" has SMS blocking...");
		System.out.println("**** "+lMDN+" has Message Blocking ? :: "+messageSupport.hasMessageBlocking(lMDN));
		System.out.println("Done");
		System.exit(0);
	}
}
