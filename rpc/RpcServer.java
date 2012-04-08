package rpc;

import identifiers.IPP;
import identifiers.SID;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import rpc.message.RpcMessage;
import rpc.message.RpcMessageCall;
import rpc.message.RpcMessageReply;
import server.SessionTable;
import server.SessionTable.Entry;
import server.SimpleDB;

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
                IPP recvIPP = new IPP(recvPkt.getAddress(), recvPkt.getPort());
                SimpleDB.getInstance().putLocalMember(recvIPP);
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
                    case RpcMessage.NOOP:
                        reply = NoOp(recvMessage);
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
        SID sid = (SID)call.getArguments().get(0);
        int changeCount = (Integer)call.getArguments().get(1);
        SessionTable table = SessionTable.getInstance();

        Entry entry = table.get(sid, changeCount);
        ArrayList<Object> results = null;
        if(entry.version >= changeCount) {
            results = new ArrayList<Object>();
            results.add(entry.message);
            results.add(entry.expiration);
        }
        return new RpcMessageReply(call.getCallID(), results, RpcServer.getInstance().getIPPLocal());
    }

    public RpcMessageReply SessionWrite(RpcMessageCall call) {
        SID sid = (SID)call.getArguments().get(0);
        int changeCount = (Integer)call.getArguments().get(1);
        String data = (String)call.getArguments().get(2);
        long discardTime = (Long)call.getArguments().get(3);
        SessionTable table = SessionTable.getInstance();

        table.put(sid, new Entry(changeCount, data, discardTime));
        return new RpcMessageReply(call.getCallID(), new ArrayList<Object>(), RpcServer.getInstance().getIPPLocal());
    }

    public RpcMessageReply SessionDelete(RpcMessageCall call) {
        SID sid = (SID)call.getArguments().get(0);
        int changeCount = (Integer)call.getArguments().get(1);
        SessionTable table = SessionTable.getInstance();
        table.destroySession(sid, changeCount);

        return new RpcMessageReply(call.getCallID(), new ArrayList<Object>(), RpcServer.getInstance().getIPPLocal());
    }

    public RpcMessageReply NoOp(RpcMessageCall call) {
        return new RpcMessageReply(call.getCallID(), new ArrayList<Object>(), RpcServer.getInstance().getIPPLocal());
    }

    /**
     * Gets a unique callID
     * @return
     */
    public int callID() {
        if(theServer == null)
            theServer = new RpcServer();
        return callIDCounter++;
    }

    /**
     * Gets a the port the UDP server is running on
     * @return
     */
    public int getPort() {
        if(theServer == null)
            theServer = new RpcServer();
        return rpcSocket.getLocalPort();
    }

    /**
     * gets the local IPP
     * @return
     */
    public IPP getIPPLocal() {
        if(theServer == null)
            theServer = new RpcServer();
        return ippLocal;
    }
}
