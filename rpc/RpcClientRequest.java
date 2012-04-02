package rpc;

import identifiers.IPP;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

import rpc.message.RpcMessage;
import rpc.message.RpcMessageCall;
import rpc.message.RpcMessageReply;

public class RpcClientRequest extends Thread {
    private int opCode;
    private ArrayList<IPP> ippList;
    private ArrayList<Object> arguments;
    private ArrayList<Object> results = null;

    public RpcClientRequest(ArrayList<IPP> ippList, int opCode, ArrayList<Object> arguments) {
        super("ClientRequest");
        this.ippList = ippList;
        this.opCode = opCode;
        this.arguments = arguments;
    }

    @Override
    public void run() {
        int callID = RpcServer.getInstance().callID();

        DatagramSocket rpcSocket = null;
        try {
            rpcSocket = new DatagramSocket();
        } catch (SocketException e1) {
            System.err.println("Failed to create socket");
            e1.printStackTrace();
        }
        RpcMessageCall outMsg = new RpcMessageCall(callID, opCode, arguments);
        byte[] outBuf = outMsg.toByteStream();
        for( IPP address : ippList ) {
            DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, address.getIp(), address.getPort());
            try {
                rpcSocket.send(sendPkt);
            } catch (IOException e) {
                // TODO: Smarty smart stuff
                e.printStackTrace();
            }
        }
        byte [] inBuf = new byte[RpcMessage.BUFFER_SIZE];
        DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
        RpcMessageReply inMsg = null;
        try {
            do {
                recvPkt.setLength(inBuf.length);
                rpcSocket.receive(recvPkt);
                inMsg = (RpcMessageReply) RpcMessage.readByteStream(inBuf);
            } while( inMsg.getCallID() != callID );
        } catch(InterruptedIOException iioe) {
            // timeout
            inMsg = null;
        } catch(IOException ioe) {
            // other error
        }
        results = inMsg.getResults();
    }

    /**
     * Returns the result of the request, or NULL if the request has not been completed
     */
    public ArrayList<Object> getResults() {
        return results;
    }
}
