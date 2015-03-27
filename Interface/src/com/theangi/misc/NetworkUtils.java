package com.theangi.misc;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class NetworkUtils {
	
	/**
	 * Ritorna tutte le interfacce di rete trovate
	 * @return
	 */
	public static ArrayList<String> getAllInterfaces(){
	
		ArrayList<String> lista = new ArrayList<String>();
		
		Enumeration<NetworkInterface> nInterfaces = null;
		try {
			nInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	    while (nInterfaces.hasMoreElements()) {
	        Enumeration<InetAddress> inetAddresses = nInterfaces.nextElement().getInetAddresses();
	        while (inetAddresses.hasMoreElements()) {
	            String address = inetAddresses.nextElement().getHostAddress();
	            lista.add(address);
	        }
	    }
	    
	    return lista;
	}
	
	/**
	 * Cerca nelle interfacce di rete il vero IP di questo sistema
	 * @return
	 */
	public static String getLocalIP(){
		
		ArrayList<String> lista = NetworkUtils.getAllInterfaces();
		
		for (String s : lista) {
			if(s.startsWith("192.168")){
				return s;
			}
		}
		
		/*Errore*/
		return "";
	}
	
	/**
	 * Permette di trovare tutti i peer presenti nella rete.
	 * In particolare, ritorna il nome dei servizi registrati
	 * sui relativi peer, in modo che Naming.lookup() sia subito
	 * possibile. Specificare first available garantisce il ritorno di un ArrayList
	 * con UN SOLO elemento contenente il primo nome disponibile libero.
	 * @param firstAvailable 
	 * @return l'elenco dei peer nella rete
	 */
    public static ArrayList<String> findPeers(boolean firstAvailable){
    	
    	//System.out.println("=== findPeers ===");
    	ArrayList<String> elenco = new ArrayList<String>();
    	
    	for(int i=1;i<255;i++){
			
    		String who = Constants.PREFIX_PEER + i;
			
			try {
				
				/*Per farlo funzionare fai ipconfig sull'altro pc e guarda*/
				//Naming.lookup("//miodesktop:" + Constants.RMI_PORT + "/" + who);	//questo funziona nella realtà
				Naming.lookup(who);													//questo funziona sul localhost
				
				/*Se non lancia eccezione naming.lookup, significa esiste!*/
				elenco.add(who);
				
			} catch (MalformedURLException e) {
				System.out.println("Malformed");
			} catch (RemoteException | NotBoundException e2) {
				//e.printStackTrace();
				
				if(firstAvailable){
					
					/*Significa voglio solo trovare il primo disponibile*/
					elenco.clear();
					elenco.add(who);
					return elenco;
				}
				
			} catch (Exception e){
				System.out.println("Eccezione generica");
			}
    	}
    	//System.out.println("=== /findPeers ===");
    	//Utils.stampaLogga("In fase di lookup ho trovato " + elenco.size() + " elementi");
    	
    	return elenco;
    }
    
    /**
     * checkHosts("192.168.0");
     * @param subnet
     * @return
     */
    public static ArrayList<String> findHostsSerially(String subnet){
    	int timeout=10;
    	ArrayList<String> tmp = new ArrayList<String>();
    	
    	for (int i=1;i<254;i++){
    		String host=subnet + "." + i;
    		
    		try {
				if (InetAddress.getByName(host).isReachable(timeout)){
					//System.out.println(host + " is reachable");
					tmp.add(host);
					//System.out.print(tmp.size());
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	
    	return tmp;
    }
    
    public static ArrayList<String> findREALPeers(){
    	
    	ArrayList<String> tmp = new ArrayList<String>();
    	
    	/*Prendo tutti gli ip che ho trovato*/
    	HashMap<String,String> lista = findREALHosts();
    	
    	for (Map.Entry<String, String> entry : lista.entrySet()) {
    		/*Gli indirizzi dovrebero già avere la "/" davanti ai numeri!*/
    		
			try {
				
				String url = entry.getKey() + ":" + Constants.RMI_PORT + "/" + entry.getValue();
				System.out.println("RMI provo a connettermi a " + url);
				
				Naming.lookup(url);
				
				System.out.println("MIRACOLOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");
				tmp.add(entry.getValue());
				
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				//e.printStackTrace();
				System.out.println("L'host è raggiungibile ma non è RMI!");
			} catch (NotBoundException e) {
				e.printStackTrace();
			}
		}
    	
    	return tmp;
    	
    	
    }
    
    /**
     * Lancia esattamente 254 thread che cercando gli altrettati nodi nella rete
     * non viene cercato il nodo0 perchè questo corrsiponderebbe all'indirizzo speciale di broadcast
     * e non viene nemmeno cercato il 255
     * 
     */
    private static HashMap<String,String> findREALHosts(){
    	
    	ArrayList<NetworkRunnable> threads = new ArrayList<NetworkRunnable>();
    	
    	/*Lancio 255 Thread paralleli*/
    	for(int i=0;i<254;i++){
    		threads.add(new NetworkRunnable(i, NetworkUtils.getLocalIP()));
    	}
    	
    	System.out.println("1__________Fatto e partiti i thread");
    	
    	/*Aspetto tutti abbiano finito*/
    	for(int i=0;i<254;i++){
    		try {
				threads.get(i).t.join();
			} catch (InterruptedException e) {
				System.out.println("__________Il Thread " + i + " non è morto bene!");
				e.printStackTrace();
			}
    	}
    	
    	System.out.println("2__________Sono morti tutti");
    	
    	/*Colleziono i risultati*/
    	HashMap<String,String> elenco = new HashMap<String,String>();
    	
    	for(int i=0;i<254;i++){
    		String tmp1 = threads.get(i).getAddress();
    		String tmp2 = threads.get(i).getNomePeer();
    		if(tmp1!=null && tmp2!=null){
    			elenco.put(tmp1, tmp2);
    		}
    	}
    	
    	System.out.println("3__________Trovati " + elenco.size() + " elementi!!!!");
    	return elenco;
    	
    }
}
