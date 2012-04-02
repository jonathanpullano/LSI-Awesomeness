package rpc.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public abstract class RpcMessage implements Serializable {
    private static final long serialVersionUID = -6093564626696863788L;
    public final static int BUFFER_SIZE = 1024;
    public final static String LENGTH_EXCEEDED = "Message Exceeds Maximum Packet Length";

    //OpCodes
    final public static int READ = 0;
    final public static int WRITE = 1;
    final public static int DELETE = 2;
    final public static int NOOP = 3;


    public int callID;

    public RpcMessage(int callID) {
        this.callID = callID;
    }

    public int getCallID() {
        return callID;
    }

    public void setCallID(int callID) {
        this.callID = callID;
    }

    public byte[] toByteStream() {
        byte[] bytes = null;
        try {
            ByteArrayOutputStream bStream = new ByteArrayOutputStream();
            ObjectOutputStream oStream = new ObjectOutputStream(bStream);
            oStream.writeObject(this);
            bytes = bStream.toByteArray();
        } catch(Exception e) {
            System.err.println("Error writing RpcMessage to byte stream");
            e.printStackTrace();
        }
        return bytes;
    }

    public static RpcMessage readByteStream(byte[] inBuf) {
        RpcMessage msg = null;
        try {
            ByteArrayInputStream bStream = new ByteArrayInputStream(inBuf);
            ObjectInputStream iStream = new ObjectInputStream(bStream);
            msg = (RpcMessage)iStream.readObject();
        } catch(Exception e) {
            System.err.println("Error reading byte stream into RpcMessage");
            e.printStackTrace();
        }
        return msg;
    }

    public boolean validatePacket() {
        if(this.toByteStream().length > BUFFER_SIZE)
            throw new RuntimeException(LENGTH_EXCEEDED);
        return true;
    }
 }
