package request;

import identifiers.IPP;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.UUID;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;

public class simpleDB {

	private static boolean DEBUG = true;

	// AWS Credentials
	static String sAccessKey = "AKIAJ2D62ULWYRFXSLRA";
	static String sSecretKey = "+y6U58eLA0+l/JxM2YxwVMHa3o43unlCGs5GvQjQ";

	// Authenticate AWS account
	static BasicAWSCredentials oAWSCredentials = new BasicAWSCredentials(sAccessKey, sSecretKey);
	static AmazonSimpleDBClient awsSimpleDBClient = new AmazonSimpleDBClient(oAWSCredentials);

	public static void createTable(String domain){
		if(DEBUG) System.out.println("Connecting and creating domain (" + domain + ")"); 

		awsSimpleDBClient.createDomain(new CreateDomainRequest(domain));
	
	}
	
	public static void put(String domain, IPP ipp){
		ArrayList<ReplaceableAttribute> newAttributes = new ArrayList<ReplaceableAttribute>();
		Attribute attr = new Attribute();
		newAttributes.add(new ReplaceableAttribute("Ipp", ipp.toString(), false));
		PutAttributesRequest newRequest = new PutAttributesRequest();
		newRequest.setDomainName(domain);
		newRequest.setItemName(UUID.randomUUID().toString());
		 newRequest.setAttributes(newAttributes);
		 
		 awsSimpleDBClient.putAttributes(newRequest);	 
	}
	
	//TODO main calls createTable using CS5300PROJECT1BSDBMbrList
	public static void main(String[] args) {
        String domain = "CS5300PROJECT1BSDBMbrList";
        InetAddress ip = null;
		try {
			ip = InetAddress.getLocalHost();
	        System.out.println(ip.getHostAddress());
	        IPP ippPrime = new IPP(ip, 5555);
	        createTable(domain);
	        put(domain, ippPrime);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}



        
    }
}
