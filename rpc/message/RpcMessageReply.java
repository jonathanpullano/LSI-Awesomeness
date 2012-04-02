package rpc.message;

import java.util.ArrayList;

public class RpcMessageReply extends RpcMessage {
    private static final long serialVersionUID = -5962879604671075016L;
    private ArrayList<Object> results;

    public RpcMessageReply(int callID, ArrayList<Object> results) {
        super(callID);
        this.results = results;
        validatePacket();
    }

    public ArrayList<Object> getResults() {
        return results;
    }

    public void setResults(ArrayList<Object> results) {
        this.results = results;
    }
}
