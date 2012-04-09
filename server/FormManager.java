package server;

import identifiers.FormData;

import java.util.HashMap;

/**
 * Tracks the FormData object so we can write debugging stuff to it
 * NB: ALL CALLS TO THIS CLASS MUST COME FROM THE SAME THREAD.
 * @author jonathanpullano
 *
 */
public class FormManager {
    private static FormManager theManager = new FormManager();
    private HashMap<Long, FormData> formData = new HashMap<Long, FormData>();
    
    private FormManager() {}
    
    public void newRequest() {
        formData.put(Thread.currentThread().getId(), new FormData());
    }
    
    public void endRequest() {
        formData.remove(Thread.currentThread().getId());
    }
    
    public FormData getData() {
        return formData.get(Thread.currentThread().getId());
    }
    
    public static FormManager getInstance() {
        return theManager;
    }
}
