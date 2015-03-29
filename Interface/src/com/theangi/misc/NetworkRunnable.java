package com.theangi.misc;

import java.net.InetAddress;

public class NetworkRunnable implements Runnable{

	Thread t;
	
	final private int i;
	final private String myIP;
	
	private String addressFound = null;
	private String nomePeer = null;
	
	
	public NetworkRunnable(int i, String myIP) {
		super();
		this.i = i;
		this.myIP = myIP;
		
		/*Faccio partire un thread!*/
		t = new Thread(this);
		t.start();
	}

	@Override
	public void run() {
		
		/*Non esiste nodo0 e non esiste nodo255*/
		int numNodo = i+1;
		
		if(numNodo==0 || numNodo==255){
			Utils.stampaLogga("Errore! non devo cercare nodo" + numNodo);
			return;
		}

		
		String who = Constants.PREFIX_PEER + numNodo;
		
		try {
			
			/*Prendo il mio indirizzo locale da stringa a classe InetAddress*/
			InetAddress localhost = InetAddress.getByName(myIP);
			
			/*Costruisco l'ip*/
			byte[] ip = localhost.getAddress();
			
			/*Modifico il 4o byte*/
			ip[3] = (byte)numNodo; //modifico il 3o byte!
			
			/*Questo è l'indirizzo finale*/
			InetAddress address = InetAddress.getByAddress(ip);
			
			/*Controllo se è raggiungibile*/
			if (address.isReachable(Constants.TIMEOUT_SEARCH_HOSTS)){
				System.out.println(who + ", ovvero " + address + ", è raggiungibile!");
				addressFound = address.toString();
				nomePeer = who;
			}
	
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}
	
	/**
	 * @return l'indirizzo IP del nodo
	 */
	public String getAddress(){		
		return addressFound;
	}

	/**
	 * @return il nome del nodo. Per scelta anchitetturale, questo è sempre nodox dove x è l'ultimo byte
	 * dell'indirizzo IP
	 */
	public String getNomePeer() {
		return nomePeer;
	}
}
