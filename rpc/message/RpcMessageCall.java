package rpc.message;

import java.util.ArrayList;

public class RpcMessageCall extends RpcMessage {
    private int opCode;
    private ArrayList<Object> arguments; //TODO: Are strings better?

    public RpcMessageCall(int callID, int opCode, ArrayList<Object> arguments) {
        super(callID);
        this.opCode = opCode;
        this.arguments = arguments;
    }

    public ArrayList<Object> getArguments() {
        return arguments;
    }

    public void setArguments(ArrayList<Object> arguments) {
        this.arguments = arguments;
    }

    public int getOpCode() {
        return opCode;
    }

    public void setOpCode(int opCode) {
        this.opCode = opCode;
    }
}
