package rpc;

import identifiers.IPP;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;

import rpc.message.RpcMessage;
import rpc.message.RpcMessageCall;
import rpc.message.RpcMessageReply;
import server.SimpleDB;

public class RpcClientRequest extends Thread {
    private int opCode;
    private Collection<IPP> ippList;
    private ArrayList<Object> arguments;
    private RpcMessageReply results = null;

    public final static int SOCKET_TIMEOUT = 2000;
    private static final boolean DEBUG = true;

    public RpcClientRequest(Collection<IPP> ippList, int opCode, ArrayList<Object> arguments) {
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
            rpcSocket.setSoTimeout(SOCKET_TIMEOUT);
        } catch (SocketException e1) {
            //Could not open UDP socket, so let's just pretend the request failed, okay? :(
            System.err.println("Failed to create socket");
            e1.printStackTrace();
            results = null;
            return;
        }
        RpcMessageCall outMsg = new RpcMessageCall(callID, opCode, arguments);
        byte[] outBuf = outMsg.toByteStream();
        for( IPP address : ippList ) {
            //TODO: Are there cases where the example code breaks? :O
            if(DEBUG) System.out.println("Sending a request to:" + address);
            DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, address.getIp(), address.getPort());
            try {
                rpcSocket.send(sendPkt);
            } catch (IOException e) {
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
            if(DEBUG) System.out.println("Requests must have timed out D:");
            //All sent messages must have timed out
            for(IPP address : ippList)
                SimpleDB.getInstance().deleteLocalMember(address);
            results = null;
            return;
        } catch(IOException ioe) {
            // other error
        }
        results = inMsg;
    }

    /**
     * Returns the result of the request
     */
    public RpcMessageReply getReply() {
        return results;
    }
    

}
