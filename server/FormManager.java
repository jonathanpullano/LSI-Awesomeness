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
    private HashMap<Long, FormData> formData;
    
    private FormManager() {}
    
    public synchronized void newRequest() {
        formData.put(Thread.currentThread().getId(), new FormData());
    }
    
    public synchronized void endRequest() {
        formData.remove(Thread.currentThread().getId());
    }
    
    public synchronized FormData getData() {
        return formData.get(Thread.currentThread().getId());
    }
    
    public static FormManager getInstance() {
        return theManager;
    }
}
