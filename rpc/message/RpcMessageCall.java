package rpc.message;

import identifiers.IPP;
import identifiers.SID;

import java.util.ArrayList;
import java.util.Collection;

import rpc.RpcClientRequest;
import server.SessionTable;

public class RpcMessageCall extends RpcMessage {
    private static final long serialVersionUID = -424650587395225785L;
    private int opCode;
    private ArrayList<Object> arguments;

    public RpcMessageCall(int callID, int opCode, ArrayList<Object> arguments) {
        super(callID);
        this.opCode = opCode;
        this.arguments = arguments;
        validatePacket();
    }

    public ArrayList<Object> getArguments() {
        return arguments;
    }

    public int getOpCode() {
        return opCode;
    }

    private static ArrayList<Object> send(Collection<IPP> ippList, int opCode, ArrayList<Object> arguments) {
        RpcClientRequest client = new RpcClientRequest(ippList, opCode, arguments);
        client.start();
        while(client.getState() != Thread.State.TERMINATED);
        return client.getResults();
    }
    
    public static ReadResult SessionRead(ArrayList<IPP> ippList, SID sid, int changeCount) {
        ArrayList<Object> arguments = new ArrayList<Object>();
        arguments.add(sid);
        arguments.add(changeCount);
        ArrayList<Object> results = send(ippList, RpcMessage.READ, arguments);
        if(results == null)
            return null;
        ReadResult readResult = new ReadResult((String)results.get(0), (Long)results.get(1));
        SessionTable.getInstance().cache(sid, readResult, changeCount);
        return readResult;
    }

    public static boolean SessionWrite(Collection<IPP> ippList, SID sid, int changeCount, long discardTime) {
        ArrayList<Object> arguments = new ArrayList<Object>();
        arguments.add(sid);
        arguments.add(changeCount);
        arguments.add(discardTime);
        return send(ippList, RpcMessage.WRITE, arguments) != null;
    }
    
    public static boolean SessionWrite(IPP ipp, SID sid, int changeCount, long discardTime) {
        ArrayList<IPP> ippList = new ArrayList<IPP>();
        ippList.add(ipp);
        return SessionWrite(ippList, sid, changeCount, discardTime);
    }

    public static boolean SessionDelete(Collection<IPP> ippList, SID sid, int changeCount) {
        ArrayList<Object> arguments = new ArrayList<Object>();
        arguments.add(sid);
        arguments.add(changeCount);
        return send(ippList, RpcMessage.DELETE, arguments) != null;
    }

    public static boolean NoOp(IPP ipp) {
        ArrayList<IPP> ipps = new ArrayList<IPP>();
        ipps.add(ipp);
        return NoOp(ipps);
    }
    
    public static boolean NoOp(Collection<IPP> ippList) {
        ArrayList<Object> arguments = new ArrayList<Object>();
        
        return send(ippList, RpcMessage.NOOP, arguments) != null;
    }
    
    public static class ReadResult {
        String data;
        long discardTime;

        public ReadResult(String data, long discardTime) {
            this.data = data;
            this.discardTime = discardTime;
        }

        public String getData() {
            return data;
        }

        public long getDiscardTime() {
            return discardTime;
        }
    };
}
