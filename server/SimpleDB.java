package server;

import identifiers.IPP;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import rpc.RpcServer;
import rpc.message.RpcMessageCall;
import util.Configuration;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;
import com.amazonaws.services.simpledb.model.UpdateCondition;


public class SimpleDB {

    public static final String MEMBER_LIST_DOMAIN = "CS5300PROJECT1BSDBMbrList";

	private static boolean DEBUG = true;
	
	private static int ROUND_SLEEP_TIME = 5;
	
	private static BasicAWSCredentials oAWSCredentials = null;
	private static AmazonSimpleDBClient sdbc = null;
	private static String AttrName = "IPP";
	
	private static  Set<IPP> localMbrList = Collections.synchronizedSet(new HashSet<IPP>());
	 
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
		localMbrList.clear();
		
		//Read the SDBMbrList from SimpleDB.
		ArrayList<IPP> DBMbrList = getMembers();
		
		//Send a NoOp to each member RPC to each IPP in the list (except IPPself) and wait for responses. 
		//(You may want to do this more than once, to deal with dropped packets). Note that by the basic membership 
		//protocol of Section 3.9 every response will cause a server to be added to the MbrSet.
		HashSet<IPP> retrySet = new HashSet<IPP>();
		for(IPP ipp : DBMbrList) {
			if(RpcMessageCall.NoOp(ipp))
				localMbrList.add(ipp);
			else
			    retrySet.add(ipp);
		}
		for(IPP ipp : retrySet) {
            if(RpcMessageCall.NoOp(ipp))
                localMbrList.add(ipp);
        }
			
		//Add IPPself to the MbrSet.
		InetAddress ip = null;
		try {
			ip = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		IPP ipp = new IPP(ip, RpcServer.getInstance().getPort());
		localMbrList.add(ipp);
		
		
		//Write this new MbrSet into the SDBMbrList on SimpleDB.
		ArrayList<ReplaceableAttribute> newAttributes = new ArrayList<ReplaceableAttribute>();
		
		newAttributes.add(new ReplaceableAttribute(AttrName,  trimAndToString(localMbrList), false));
		PutAttributesRequest newRequest = new PutAttributesRequest();
		
		UpdateCondition expected = new UpdateCondition();
		expected.setName(AttrName);
		expected.setValue(trimAndToString(DBMbrList));
		
		newRequest.setDomainName(MEMBER_LIST_DOMAIN);
		newRequest.setItemName(AttrName);
		
		newRequest.setExpected(expected);
		newRequest.setAttributes(newAttributes);
		
		try {
			sdbc.putAttributes(newRequest);
		} catch(Exception e){
			if(DEBUG) System.out.println("Cought exception.. Probably an update exception");
		}
	}

	public void deleteLocalMember(IPP ipp){
		localMbrList.remove(ipp);
	}
	
	public void putLocalMember(IPP ipp){
		localMbrList.add(ipp);
	}
	
	public String trimAndToString(Set<IPP> localMbrList2){
		return localMbrList2.toString().replace("[", "").replace("]", "");
	}
	
	public String trimAndToString(ArrayList<IPP> list){
		return list.toString().replace("[", "").replace("]", "");
	}
	public ArrayList<IPP> getLocalMembers(){
		ArrayList<IPP> result = new ArrayList<IPP>();
		result.addAll(localMbrList);
		return result;
	}
	
	public ArrayList<IPP> getMembers(){
		ArrayList<IPP> servers = new ArrayList<IPP>();
		String query = "select " + AttrName + " from " + MEMBER_LIST_DOMAIN;
		SelectRequest selectRequest = new SelectRequest(query);
		SelectResult result = sdbc.select(selectRequest);
		List<Item> items = result.getItems();
		
		/**
		 * We always use one attribute so we get 0th item and get the 0th attribute 
		 */
		String row = items.get(0).getAttributes().get(0).getValue();
		String[] serverList = row.split(", ");
		
		for(String ipp : serverList){
			IPP ippMember = IPP.getIPP(ipp);
			servers.add(ippMember);
		}
		return servers;
	}

	public void listDomains(PrintWriter out) {
	    for(String domainName : sdbc.listDomains().getDomainNames()){
	    	if(DEBUG) System.out.println("Domain: " + domainName);
            out.println("Domain: " + domainName);
        }
	}
	
	public void listDomains() {
	    for(String domainName : sdbc.listDomains().getDomainNames()){
	    	if(DEBUG) System.out.println("Domain: " + domainName);
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

	public void setLocalMbrList(HashSet<IPP> localMbrList) {
		SimpleDB.localMbrList = localMbrList;
	}
	
	public void run() {
	    memberRefresh();
	    while(true) {
    		try {
                Thread.sleep(ROUND_SLEEP_TIME);
    			Random generator = new Random();
    			double probOfRefresh = 1.0/localMbrList.size();
    			double rand = generator.nextDouble();
    			
    			if(rand <= probOfRefresh)
    				memberRefresh();
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
	    }
	}
}
