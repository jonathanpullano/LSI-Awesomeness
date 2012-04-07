package rpc.message;

import identifiers.IPP;

import java.util.ArrayList;

public class RpcMessageReply extends RpcMessage {
    private static final long serialVersionUID = -5962879604671075016L;
    private IPP server;
    private ArrayList<Object> results;

    public RpcMessageReply(int callID, ArrayList<Object> results, IPP server) {
        super(callID);
        this.results = results;
        this.server = server;
        validatePacket();
    }

    public ArrayList<Object> getResults() {
        return results;
    }
    
    public IPP getServer() {
        return server;
    }
}
