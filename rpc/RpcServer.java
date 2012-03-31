package rpc;

import identifiers.IPP;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

import rpc.message.RpcMessage;
import rpc.message.RpcMessageCall;
import rpc.message.RpcMessageReply;

public class RpcServer extends Thread {

    private static RpcServer theServer;
    
    private DatagramSocket rpcSocket;
    private static int callIDCounter;

    /**
     * Private Constructor (Singleton Pattern)
     * Use getInstance to access
     */
    private RpcServer() {
        try {
            rpcSocket = new DatagramSocket();
            callIDCounter = 10000 * rpcSocket.getLocalPort();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            while(true) {
                byte[] inBuf = new byte[RpcMessage.BUFFER_SIZE];
                DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
                rpcSocket.receive(recvPkt);
                InetAddress returnAddr = recvPkt.getAddress();
                int returnPort = recvPkt.getPort();
                RpcMessageCall recvMessage = (RpcMessageCall) RpcMessage.readByteStream(inBuf);
                int operationCode = recvMessage.getOpCode(); // get requested operationCode
                byte[] outBuf = null;
                switch( operationCode ) {
                case RpcMessage.READ:
                    outBuf = SessionRead(recvPkt.getData(), recvPkt.getLength());
                    break;
                }
                // here outBuf should contain the callID and results of the call
                DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length,
                        returnAddr, returnPort);
                rpcSocket.send(sendPkt);
            }
        } catch(Exception e) {
            //TODO: smart exception handly stuff
            e.printStackTrace();
        }
    }

    public static RpcServer getInstance() {
        if(theServer == null)
            theServer = new RpcServer();
        return theServer;
    }
    
    //-----------------------------------------------------------
    //NB: This function may change completely in the near future
    //-----------------------------------------------------------
    public byte[] SessionRead(byte[] data, int length) {
        return new byte[length];
    }

    public int callID() throws RuntimeException {
        if(theServer != null)
            return callIDCounter++;
        else throw new RuntimeException("Need to start the server before creating clients");
    }
}
