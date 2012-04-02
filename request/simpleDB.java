package request;

import identifiers.IPP;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
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

    public static final String MEMBER_LIST_DOMAIN = "CS5300PROJECT1BSDBMbrList";

	private static boolean DEBUG = true;

	private static BasicAWSCredentials oAWSCredentials = null;
	private static AmazonSimpleDBClient sdbc = null;
	private static String ItemName = "IPP";
	private static String AttrName = "IPP";
	public SimpleDB(){
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


	public void deleteMember(String domain, IPP ipp){

		DeleteAttributesRequest deleteAttributesRequest = new DeleteAttributesRequest(domain, ItemName);

		//sdbc.deleteAttributes(deleteAttributesRequest );
	}

	public boolean exists(String domain, IPP ipp){
		boolean exists = false;
		//Check if its in the list first
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

	public void putMember(String domain, IPP ipp){
		if(DEBUG) System.out.println("Adding IP-P " + ipp.toString() + " to " + domain + "");

		if(exists(domain, ipp)){
			System.out.println("Member already exists in membership list.");
			return;
		}

		ArrayList<ReplaceableAttribute> newAttributes = new ArrayList<ReplaceableAttribute>();
		newAttributes.add(new ReplaceableAttribute(AttrName, ipp.toString(), false));
		PutAttributesRequest newRequest = new PutAttributesRequest();

		newRequest.setDomainName(domain);
		newRequest.setItemName(UUID.randomUUID().toString());
		newRequest.setAttributes(newAttributes);


		sdbc.putAttributes(newRequest);
	}

	public List<Item> getMembersDetails(String domain){

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
		System.out.println(items);

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
            out.println("Domain: " + domainName);
        }
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
		SimpleDB db = new SimpleDB();
		try {
			ip = InetAddress.getLocalHost();
			System.out.println(ip.getHostAddress());
			IPP ippPrime = new IPP(ip, 3555);
			//db.deleteDomain(domain);
			db.createDomain(domain);
			db.putMember(domain, ippPrime);
			db.listDomains();
			System.out.println(db.getMembers(domain));
			//db.deleteMember(domain, ippPrime);

		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
}
