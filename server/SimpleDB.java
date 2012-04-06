package server;

import identifiers.IPP;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import util.Configuration;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;

public class SimpleDB {

    public static final String MEMBER_LIST_DOMAIN = "SDBMbrList";

	private static boolean DEBUG = false;

	private static BasicAWSCredentials oAWSCredentials = null;
	private static AmazonSimpleDBClient sdbc = null;
	private static String AttrName = "IPP";
	
	private static HashSet<String> mbrList = new HashSet<String>();
	
	private static SimpleDB db = new SimpleDB();

	private SimpleDB() {
		oAWSCredentials = new BasicAWSCredentials(getKey(), getSecret());
		sdbc = new AmazonSimpleDBClient(oAWSCredentials);
	}

	public void createDomain(String domain){
		if(DEBUG) System.out.println("Connecting and creating domain (" + domain + ")");

		sdbc.createDomain(new CreateDomainRequest(domain));
	}

	public void deleteDomain(String domain){
		if(DEBUG) System.out.println("Deleting domain ("+domain +")");
		DeleteDomainRequest deleteDomainRequest = new DeleteDomainRequest();
		deleteDomainRequest.setDomainName(domain);
		sdbc.deleteDomain(deleteDomainRequest);
	}

	public void memberRefresh(){
		//Set the local MbrSet to empty.
		mbrList.clear();
		
		//Read the SDBMbrList from SimpleDB.
		ArrayList<String> myMbrList = getMembers(MEMBER_LIST_DOMAIN);
		
		//Send a NoOp to each member RPC to each IPP in the list (except IPPself) and wait for responses. 
		//(You may want to do this more than once, to deal with dropped packets). Note that by the basic membership 
		//protocol of Section 3.9 every response will cause a server to be added to the MbrSet.
		for(String member : myMbrList){
			IPP localMember = IPP.getIPP(member);
		}
			
		
		//Add IPPself to the MbrSet.
		
		//Write this new MbrSet into the SDBMbrList on SimpleDB.
		
	}
	
	public void deleteDBMember(String domain, IPP ipp){
		String membersUUID = getMembersUUID(domain, ipp);
		if(membersUUID == null){
			if(DEBUG) System.out.println("No such member found (" + ipp.toString() + ")");
			return;
		} else
			if(DEBUG) System.out.println("Deleting member (" + ipp.toString() + ")");

		DeleteAttributesRequest deleteAttributesRequest = new DeleteAttributesRequest(domain, getMembersUUID(domain, ipp));
		sdbc.deleteAttributes(deleteAttributesRequest );
	}

	public void deleteLocalMember(IPP ipp){
		//TODO delete local member from list
	}
	
	public void putLocalMember(IPP ipp){
		//TODO put local member from list
	}
	
	private String getMembersUUID(String domain, IPP ipp){
		List<Item> members = getMembersDetails(domain);

		for(Item member : members){
			List<Attribute> attrs = member.getAttributes();
			for(Attribute attr : attrs){
				if(ipp.toString().equals(attr.getValue()))
					return member.getName();
			}
		}
		return null;
	}

	private boolean exists(String domain, IPP ipp){
		boolean exists = false;
		List<Item> members = getMembersDetails(domain);
		for(Item member : members){
			List<Attribute> attrs = member.getAttributes();
			for(Attribute attr : attrs){
				if(ipp.toString().equals(attr.getValue()))
					exists = true;
			}
		}
		return exists;
	}

	public void putDBMember(String domain, IPP ipp){
		if(exists(domain, ipp)){
			if(DEBUG) System.out.println("Member already exists in membership list.");
			return;
		}else
			if(DEBUG) System.out.println("Adding IP-P " + ipp.toString() + " to " + domain + "");


		ArrayList<ReplaceableAttribute> newAttributes = new ArrayList<ReplaceableAttribute>();
		newAttributes.add(new ReplaceableAttribute(AttrName, ipp.toString(), false));
		PutAttributesRequest newRequest = new PutAttributesRequest();

		newRequest.setDomainName(domain);
		newRequest.setItemName(UUID.randomUUID().toString());
		newRequest.setAttributes(newAttributes);


		sdbc.putAttributes(newRequest);
	}

	private List<Item> getMembersDetails(String domain){

		String query = "select * from " + domain;
		SelectRequest selectRequest = new SelectRequest(query);
		SelectResult result = sdbc.select(selectRequest);
		List<Item> items = result.getItems();
		return items;
	}

	public ArrayList<String> getMembers(String domain){

		String query = "select * from " + domain;
		SelectRequest selectRequest = new SelectRequest(query);
		SelectResult result = sdbc.select(selectRequest);
		List<Item> items = result.getItems();
		if(DEBUG) System.out.println(items);

		ArrayList<String> serverList = new ArrayList<String>();

		for(Item it : items){
			List<Attribute> attrs = it.getAttributes();
			for(Attribute attr : attrs)
				serverList.add(attr.getValue());
		}
		return serverList;
	}

	public void listDomains(PrintWriter out) {
	    for(String domainName : sdbc.listDomains().getDomainNames()){
	    	if(DEBUG) System.out.println("Domain: " + domainName);
            out.println("Domain: " + domainName);
        }
	}

	private String getKey () {
		Configuration config = Configuration.getInstance();
		if(DEBUG) System.out.println("Got accessKey: " + config.getProperty("accessKey"));
		return config.getProperty("accessKey");
	}

	private String getSecret () {
		Configuration config = Configuration.getInstance();
		if(DEBUG) System.out.println("Got secretKey: " + config.getProperty("secretKey"));
		return config.getProperty("secretKey");
	}

	public static SimpleDB getInstance() {
	    return db;
	}

	public static HashSet<String> getMbrList() {
		return mbrList;
	}

	public static void setMbrList(HashSet<String> mbrList) {
		SimpleDB.mbrList = mbrList;
	}

	public ArrayList<IPP> getMemberIpps() {
	    ArrayList<IPP> memberSet = new ArrayList<IPP>();
	    ArrayList<String> stringSet = getMembers(MEMBER_LIST_DOMAIN);
	    for(String ippString : stringSet)
	        memberSet.add(IPP.getIPP(ippString));
	    return memberSet;
	}
}
