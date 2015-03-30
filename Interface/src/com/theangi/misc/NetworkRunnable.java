package com.theangi.misc;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.theangi.myinterfaces.MyInterface;

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
			
		/*Prendo il mio indirizzo locale da stringa a classe InetAddress*/
		InetAddress localhost;
		try {
			localhost = InetAddress.getByName(myIP);
			
			/*Costruisco l'ip*/
			byte[] ip = localhost.getAddress();
			
			/*Modifico il 4o byte*/
			ip[3] = (byte)numNodo; //modifico il 3o elemento del vettore!
			
			/*Questo è l'indirizzo finale*/
			InetAddress address = InetAddress.getByAddress(ip);
			
			
			try {
				
				/*Controllo se è raggiungibile*/
				if (address.isReachable(Constants.TIMEOUT_SEARCH_HOSTS)){
					
					addressFound = address.toString();
					nomePeer = who;
					
					/*Provo a collegarmi*/
					String url = "/" + address.toString() + ":" + Constants.RMI_PORT + "/" + nomePeer;
					
					Utils.stampaLogga(who + ", (" + address + ") raggiungibile, tento connessione RMI a: " + url);
					
					MyInterface remote = (MyInterface) Naming.lookup(url);
					
					Utils.stampaLogga("===> ...CONNESSIONE RIUSCITA con " + remote.whoAreYou() + "!");
					Utils.stampaLogga("Visto che sono felice dico: " + remote.toString());
					
					return;
					
				}
				
				/*Altrimenti ho già finito*/
				
			} catch (MalformedURLException e) {
				//e.printStackTrace();
			} catch (RemoteException e) {
				Utils.stampaLogga("---> ...peccato, non è RMI (" + e.getClass() + ")");
				//e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			} catch (NotBoundException e) {
				Utils.stampaLogga("---> " + nomePeer + " è raggiungibile ma non è RMI!");
			}
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		addressFound = null;
		nomePeer = null;
				
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
