package request;

import identifiers.IPP;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import util.Configuration;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;
import com.amazonaws.services.simpledb.model.UpdateCondition;

public class simpleDB {

	private static boolean DEBUG = true;
	
	static BasicAWSCredentials oAWSCredentials = null;
	static AmazonSimpleDBClient sdbc = null;
	
	public simpleDB(){
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

	public void put(String domain, IPP ipp){
		if(DEBUG) System.out.println("Adding IP-P " + ipp.toString() + " to " + domain + "");
		ArrayList<ReplaceableAttribute> newAttributes = new ArrayList<ReplaceableAttribute>();
		newAttributes.add(new ReplaceableAttribute("IPP", ipp.toString(), false));
		PutAttributesRequest newRequest = new PutAttributesRequest();
		UpdateCondition expected = new UpdateCondition();
		
		newRequest.setDomainName(domain);
		newRequest.setExpected(expected );
		newRequest.setItemName(UUID.randomUUID().toString());
		newRequest.setAttributes(newAttributes);

		sdbc.putAttributes(newRequest);	 
	}
	
	public ArrayList<String> get(String domain){
		
		String query = "select * from " + domain;
		SelectRequest selectRequest = new SelectRequest(query);
		SelectResult result = sdbc.select(selectRequest);
		List<Item> items = result.getItems();
		ArrayList<String> serverList = new ArrayList<String>();
		for(Item it : items){
			List<Attribute> attrs = it.getAttributes();
			for(Attribute attr : attrs)
				serverList.add(attr.getValue());
		}
		return serverList;
	}
	
	public void listDomains(){
		for(String domainName : sdbc.listDomains().getDomainNames()){
			System.out.println("Domain: " + domainName);
		}
	}
	
	private static String getKey () {
		Configuration config = Configuration.getInstance();
		if(DEBUG) System.out.println("Got accessKey: " + config.getProperty("accessKey"));
		return config.getProperty("accessKey");
	}

	private static String getSecret () {
		Configuration config = Configuration.getInstance();
		if(DEBUG) System.out.println("Got secretKey: " + config.getProperty("secretKey"));
		return config.getProperty("secretKey");
	}
	
	public static void main(String[] args) {
		String domain = "CS5300PROJECT1BSDBMbrList";
		InetAddress ip = null;
		simpleDB db = new simpleDB();
		try {
			ip = InetAddress.getLocalHost();
			System.out.println(ip.getHostAddress());
			IPP ippPrime = new IPP(ip, 3555);
			//db.deleteDomain(domain);
			//db.createDomain(domain);
			//db.put(domain, ippPrime);
			//db.listDomains();
			System.out.println(db.get(domain));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
}
