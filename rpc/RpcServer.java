package rpc;

import identifiers.IPP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import rpc.message.RpcMessage;
import rpc.message.RpcMessageCall;
import rpc.message.RpcMessageReply;

public class RpcServer extends Thread {

    private static RpcServer theServer;
    private DatagramSocket rpcSocket;
    private static int callIDCounter;
    private static IPP ippLocal;

    /**
     * Private Constructor (Singleton Pattern)
     * Use getInstance() to access
     */
    private RpcServer() {
        super("ServerThread");
        try {
            rpcSocket = new DatagramSocket();
            callIDCounter = 10000 * rpcSocket.getLocalPort();
            InetAddress localIP = null;
            while(localIP == null) {
                try {
                    localIP = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
            ippLocal = new IPP(localIP, rpcSocket.getLocalPort());
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
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
                RpcMessage reply = null;
                switch( operationCode ) {
                    case RpcMessage.READ:
                        reply = SessionRead(recvMessage);
                        break;
                    case RpcMessage.WRITE:
                        reply = SessionWrite(recvMessage);
                        break;
                    case RpcMessage.DELETE:
                        reply = SessionDelete(recvMessage);
                        break;
                }

                byte[] outBuf = reply.toByteStream();
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

    public RpcMessageReply SessionRead(RpcMessageCall call) {
        //TODO: SessionRead - Server Side
        int changeCount = (Integer)call.getArguments().get(0);
        System.out.println("SessionRead called");
        System.out.println("ChangeCount: " + changeCount);

        ArrayList<Object> results = new ArrayList<Object>();
        results.add("I like pie");
        results.add(1L);
        return new RpcMessageReply(call.getCallID(), results);
    }

    public RpcMessageReply SessionWrite(RpcMessageCall call) {
        //TODO: SessionWrite - Server Side
        int changeCount = (Integer)call.getArguments().get(0);
        String data = (String)call.getArguments().get(1);
        long discardTime = (Long)call.getArguments().get(3);
        ArrayList<Object> results = new ArrayList<Object>();
        return new RpcMessageReply(call.getCallID(), results);
    }

    public RpcMessageReply SessionDelete(RpcMessageCall call) {
        //TODO: SessionReply - Server Side
        int changeCount = (Integer)call.getArguments().get(0);
        ArrayList<Object> results = new ArrayList<Object>();
        System.out.println("SessionDelete called");
        return new RpcMessageReply(call.getCallID(), results);
    }

    public int callID() throws RuntimeException {
        if(theServer != null)
            return callIDCounter++;
        throw new RuntimeException("Start the server first!");
    }

    public int getPort() {
        if(rpcSocket != null)
            return rpcSocket.getLocalPort();
        throw new RuntimeException("Start the server first!");
    }

    public IPP getIPPLocal() {
        if(ippLocal != null)
            return ippLocal;
        throw new RuntimeException("Start the server first!");
    }
}
