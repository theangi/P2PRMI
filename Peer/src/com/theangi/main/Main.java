package com.theangi.main;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.theangi.client.MyClient;
import com.theangi.misc.Utils;
import com.theangi.server.MyServer;

public class Main {

	public static void main(String[] args){
		
		/*Ci provo*/
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1) {
			//e1.printStackTrace();
			Utils.stampaLogga("Impossibile usare look and feel GTK...pazienza!");
		}
		
		try {
			
			/*Come tutte le app RMI, prima avvio il server*/
			MyServer s = new MyServer();
			s.initServer();
			
			/*E poi avvio il client*/
			MyClient c = new MyClient(s.getName());
			c.initClient();
			
		} catch (Exception e) {
			System.out.println("Grave errore, dovresti cercare di spacializzare gli errori e non Exception generica");
			e.printStackTrace();
		}
	}
}
