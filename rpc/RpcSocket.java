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

public class RpcSocket {

    private DatagramSocket rpcSocket;
    private static int callIDCounter;

    public RpcSocket() {
        try {
            rpcSocket = new DatagramSocket();
            callIDCounter = 10000 * rpcSocket.getLocalPort();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    //-----------------------------------------------------------
    //NB: This function may change completely in the near future
    //-----------------------------------------------------------
    public void listen() {
        try {
            DatagramSocket rpcSocket = new DatagramSocket();
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

    //-----------------------------------------------------------
    //NB: This function may change completely in the near future
    //-----------------------------------------------------------
    public byte[] SessionRead(byte[] data, int length) {
        return new byte[length];
    }

    //-----------------------------------------------------------
    //NB: This function may change completely in the near future
    //-----------------------------------------------------------

    //
    // SessionReadClient(sessionID, sessionVersionNum)
    //   with multiple [destAddr, destPort] pairs
    //
    public RpcMessageReply SessionReadClient(ArrayList<IPP> ippList, int opCode, ArrayList<Object> arguments) {
        int callID = callID();

        RpcMessageCall outMsg = new RpcMessageCall(callID, opCode, arguments);
        byte[] outBuf = outMsg.toByteStream();
                for( IPP address : ippList ) {
                    DatagramPacket sendPkt = new DatagramPacket(outBuf, RpcMessage.BUFFER_SIZE, address.getIp(), address.getPort());
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
            } while( inMsg != null && inMsg.getCallID() != callID );
        } catch(InterruptedIOException iioe) {
            // timeout
            inMsg = null;
        } catch(IOException ioe) {
            // other error
        }
        return inMsg;
    }

    public int callID() {
        return callIDCounter++;
    }
}
