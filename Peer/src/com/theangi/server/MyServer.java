package com.theangi.server;

import java.rmi.RemoteException;
import java.util.ArrayList;

import com.theangi.misc.Constants;
import com.theangi.misc.NetworkUtils;
import com.theangi.misc.Utils;

/**
 * La classe Contiene l'entry point del server (ovvero, il main del server).
 * @author Matteo Angiari
 *
 */
public class MyServer {
	
	/**
	 * Il nome del server
	 */
	private String name;
	
	/**
	 * Inizializza il lato server
	 * <ul>
	 * 	<li>Determina il nome di questo nodo </li>
	 * 	<li>Fa partire il server rmi</li>
	 */
    public void initServer(){
        
    	/*Creo l'oggetto remoto*/
    	MyRemote server;
    	
    	
    	/* ========================== */
    	/* ========================== */
    	/* ========================== */
    	/* ========================== */
    	/* ========================== */
    	/* ========================== */
    	boolean debug = false;
    	/* ========================== */
    	/* ========================== */
    	/* ========================== */
    	/* ========================== */
    	/* ========================== */
    	/* ========================== */
    	
    	if(debug){
    		
    		/*Determino il mio nome */
        	ArrayList<String> nome = NetworkUtils.findPeers(true);
        	
        	if(nome.isEmpty() || nome==null){
        		Utils.stampaLogga("Grave errore: impossibile assegnare nome univoco al peer");
        		return;
        	}
        	
        	/*Il mio nome l'unico tornato dal metodo*/
        	name = nome.get(0);
        	
    	} else {
    	
    		/*Siamo nel mondo vero!*/
    		//ArrayList<String> findREALPeers = NetworkUtils.findREALPeers();
    		String tmp = NetworkUtils.getLocalIP();
    		
    		if(tmp.isEmpty()){
    			Utils.stampaLogga("Grave errore: impossibile assegnare nome univoco al peer");
    			return;    			
    		}
    		
    		name = Constants.PREFIX_PEER + tmp.substring(tmp.lastIndexOf('.') + 1, tmp.length());
    	}

    	Utils.stampa("=== SERVER " + name + " ===\n");
    	
		try {
			
			server = new MyRemote(name);

	    	/*Mi metto in ascolto*/
	        server.start();
			
		} catch (RemoteException e) {
			Utils.stampaLogga("Impossibile eseguire lato server!");
			e.printStackTrace();
		}
    }

    /**
     * Ritorna il nome assegnato a questo host
     * @return
     */
	public String getName() {
		return name;
	}
}
