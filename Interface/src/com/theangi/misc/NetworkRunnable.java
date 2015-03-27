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
		
		t = new Thread(this);
		t.start();
	}

	@Override
	public void run() {
		
		int numNodo = i+1;
		
		if(numNodo==0){
			System.out.println("Errore! 0");
		} else if(numNodo==1){
			System.out.println("Primo");
			
		} else if(numNodo==254){
			System.out.println("ultimo!");
		} else if(numNodo==255){
			System.out.println("Errore 255!");
		}
		
		//String host  = "192.168.1." + numNodo;
		
		int timeout=30000;
		
		//System.out.println("Lavoro sull'address " + numNodo);
		
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
			if (address.isReachable(timeout)){
				System.out.println(who + ", ovvero " + address + ", è raggiungibile!");
				addressFound = address.toString();
				nomePeer = who;
			}
	
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}
	
	public String getAddress(){		
		return addressFound;
	}

	public String getNomePeer() {
		return nomePeer;
	}
}
