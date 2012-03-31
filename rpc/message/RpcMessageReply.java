package rpc.message;

import java.util.ArrayList;

public class RpcMessageReply extends RpcMessage {
    private ArrayList<Object> results; //TODO: Are strings better?

    public RpcMessageReply(int callID, ArrayList<Object> results) {
        super(callID);
        this.results = results;
    }

    public ArrayList<Object> getResults() {
        return results;
    }

    public void setResults(ArrayList<Object> results) {
        this.results = results;
    }
}
