package server;

import identifiers.FormData;

import java.util.HashMap;

public class FormManager {
    private static FormManager theManager = new FormManager();
    private HashMap<Long, FormData> formData;
    
    private FormManager() {}
    
    public synchronized void newRequest(long threadID) {
        formData.put(threadID, new FormData());
    }
    
    public synchronized void endRequest(long threadID) {
        formData.remove(threadID);
    }
    
    public static FormManager getInstance() {
        return theManager;
    }
}
