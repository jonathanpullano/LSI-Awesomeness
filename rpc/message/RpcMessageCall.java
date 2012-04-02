package rpc.message;

import identifiers.IPP;

import java.util.ArrayList;

import rpc.RpcClientRequest;

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

    private static ArrayList<Object> send(ArrayList<IPP> ippList, int opCode, ArrayList<Object> arguments) {
        RpcClientRequest client = new RpcClientRequest(ippList, opCode, arguments);
        client.start();
        while(client.getState() != Thread.State.TERMINATED);
        return client.getResults();
    }

    public static ReadResult SessionRead(ArrayList<IPP> ippList, int sid, int changeCount) {
        ArrayList<Object> arguments = new ArrayList<Object>();
        arguments.add(sid);
        arguments.add(changeCount);
        ArrayList<Object> results = send(ippList, RpcMessage.READ, arguments);
        return new ReadResult((String)results.get(0), (Long)results.get(1));
    }

    public static ArrayList<Object> SessionWrite(ArrayList<IPP> ippList, int sid, int changeCount, long discardTime) {
        ArrayList<Object> arguments = new ArrayList<Object>();
        arguments.add(sid);
        arguments.add(changeCount);
        arguments.add(discardTime);
        return send(ippList, RpcMessage.WRITE, arguments);
    }

    public static ArrayList<Object> SessionDelete(ArrayList<IPP> ippList, int sid, int changeCount) {
        ArrayList<Object> arguments = new ArrayList<Object>();
        arguments.add(sid);
        arguments.add(changeCount);
        return send(ippList, RpcMessage.DELETE, arguments);
    }

    public static ArrayList<Object> NoOp(ArrayList<IPP> ippList) {
        ArrayList<Object> arguments = new ArrayList<Object>();
        return send(ippList, RpcMessage.NOOP, arguments);
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
