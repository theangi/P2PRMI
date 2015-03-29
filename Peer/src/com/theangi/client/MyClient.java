package com.theangi.client;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.theangi.graphics.ListTransferHandler;
import com.theangi.graphics.RootFrame;
import com.theangi.graphics.WatchDir;
import com.theangi.misc.Constants;
import com.theangi.misc.NetworkUtils;
import com.theangi.misc.Utils;
import com.theangi.myinterfaces.MyInterface;

public class MyClient{
    
    MyInterface remote;
    
    /*Info del client*/
    private final String peerId;
    private final String nomeCartellaCondivisa;
    
	/*I vari listener per capire i cambiamenti nelle 3 liste*/
	private final ListenerHelperLocalFiles helperLocalFiles;
	private final ListenerHelperRemoteHosts helperRemoteHosts;
	private final ListenerHelperRemoteFiles helperRemoteFiles;
	
	/*L'ascoltatore dei movimenti da file remoto a file locali*/
	private final ListTransferHandler listTransferHandlerR2L;
	private final ListTransferHandler listTransferHandlerL2R;
    
    /*Il frame*/
	private RootFrame root;

	/*Il contenuto delle 3 liste principali*/
	private File[] listaFileLocali;
	private String[] listaHostsRemoti;
	private String[] listaFileRemoti;
	
	/*Per ricordarsi durante il refresh l'ultimo elemento cliccato nelle liste*/
	private int currentRemoteHostClicked = -1;
	private int currentRemoteFileClicked = -1;

	/**
	 * Costruttore
	 * @param id l'id del nodo. Deve essere sotto forma di nomeX dove X è il 
	 * numero corrispondende all'ultimo byte dell'IP di questo nodo
	 */
    public MyClient(String id){
    	
    	this.peerId = id;
    	this.nomeCartellaCondivisa = Constants.PREFIX_DATABASE + this.peerId;
    	
    	helperLocalFiles = new ListenerHelperLocalFiles();
    	helperRemoteHosts = new ListenerHelperRemoteHosts();
    	helperRemoteFiles = new ListenerHelperRemoteFiles();
    	
    	listTransferHandlerR2L = new ListTransferHandler(remote,this.peerId, true);
    	listTransferHandlerL2R = new ListTransferHandler(remote,this.peerId, false);
    }
    
    /**
     * Ritorna il JFrame corrispondente
     * @return
     */
    public RootFrame getRoot() {
		return root;
	}
    
    /**
     * Imposta la lista di fileLocali
     * @param list
     */
    public void setListFileLocali(File[] list){
    	this.listaFileLocali = list; 
    }
 
    /**
     * Inizializza il client. Nel dettaglio:
     * <ul>
     * 	<li> Controlla i file nella cartella locale (o la crea se non esisteva)</li>
     *  <li> Fa partire un timer che periodicamente controlla la disponibilità di nodi remoti</li>
     *  <li> Inizializza la parte grafica</li>
     *  <li> Fa partire un whatcher che risponde alle modifiche che possono essere fatte nella cartella locale</li>
     */
    public void initClient(){
        
    	Utils.stampa("=== CLIENT " + this.peerId + " ===\n");
    	
    	/*Controllo se ho la cartella, altrimenti la creo*/
		File f = new File(this.nomeCartellaCondivisa);
		if(!f.exists()){
			Utils.stampaLogga("Ho creato la cartella condivisa poichè non esisteva");
			f.mkdirs();
		}
		
		/*Ottengo la lista dei file presenti nella cartella (anche nessuna)*/
		listaFileLocali = new File(nomeCartellaCondivisa).listFiles();
        
		/*All'inizio questi sono vuoti*/
        listaHostsRemoti = new String[0];
        listaFileRemoti = new String[0];
        
        Timer timer = new Timer();
        timer.schedule( new TimerTask() {
            public void run() {
            	
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
            	
            	/*Determino la lista di peer remoti raggiungibili*/
            	ArrayList<String> tmp;
            	
            	if(debug){
            		tmp = NetworkUtils.findPeers(false);
            	} else {
            		tmp = NetworkUtils.findREALPeers();
            	}
            	
            	/*Devo aggiornare tutto. Dunque ho varie possibilità:
            	 * 
            	 * Senza fare nulla, il numero di host remoti raggiungibili è cambiato
            	 * Senza fare nulla, i file remoti dell'host attualmente selezionati sono cambiati
            	 * Senza fare nulla, i file locali sono cambiati -> ci pensa il whatcher locale!
            	 * 
            	 * */
            	
            	
            	/* Vediamo in remoto cosa è cambiato tmp avrà sempre al suo interno il nome dell'host corrente*/
            	if(listaHostsRemoti.length!=(tmp.size()-1)){
            		
            		Utils.stampaLogga("LOOP: ho trovato " + tmp.size() + " hosts remoti");
     		        
            		/*Dall'elenco, rimuovo l'host locale. Funziona solo perchè le stringhe sono immutabili*/
            		tmp.remove(peerId);
            		listaHostsRemoti = tmp.toArray(new String[tmp.size()]);
            		tmp.add(peerId);
     		        
     		        if(listaHostsRemoti != null){
     		        	root.refreshHostRemoti(listaHostsRemoti, currentRemoteHostClicked);
     		        } else {
     		        	Utils.stampaLogga("Grave errore: non ho host remoti!");
     		        }
            	} 
            	
            	/* Se non è cambiato nulla, vediamo se avevo già cliccato qualcosa, e nel caso aggiorno quello che già l'utente vede adesso*/
            	if(currentRemoteHostClicked>=0){
            		
	        		try {
	        			
	        			Utils.stampaLogga("LOOP: aggiorno files hostremoto nell'elenco numero:" + currentRemoteHostClicked);
	        			
	        			String chi = listaHostsRemoti[currentRemoteHostClicked];
	        			
	        			/*Nel mondo locale, basta dire "nodox" e va bene*/
	        			if(!debug){
	        				/* Ma nel mondo "vero", devo ndicare /192.168.1.x:1099/nodox */
	        				chi = NetworkUtils.getRemoteIP(chi) + ":" + Constants.RMI_PORT + "/" + chi;
	        			}
	            		
	        			Utils.stampaLogga("Contatto " + chi + " per vedere se ha dei file nuovi!");
	        			
	        			/*Mi collego all'host remoto attualmente mostrato*/
	        			remote = (MyInterface) Naming.lookup(chi);
	        			
	            		/*Prendo il suo elenco di file*/
	            		ArrayList<String> tmp2 = remote.getElencoFiles();
	            		
						/*Aggiorno graficamente l'elenco*/
						if(tmp2.size() != listaFileRemoti.length){
							/*Ho appena concluso un download o un upload, non seleziono nulla*/
							Utils.stampa("--------> Ne aveva di nuovi! Figo ma dimentico cosa avevo selezionato prima");
							listaFileRemoti = tmp2.toArray(new String[tmp2.size()]);
							root.refreshFileRemoti(listaFileRemoti, -1);
						} else {
							Utils.stampa("--------> Non è cambiato un cazzo, ma aggiorno lo stesso");
							/*Lascio selezionato quello correntes*/
							root.refreshFileRemoti(listaFileRemoti, currentRemoteFileClicked);
						}
						
	        		} catch (MalformedURLException e) {
						e.printStackTrace();
						Utils.stampaLogga("Errore determinazione indirizzo host remoto");
					} catch (RemoteException e) {
						e.printStackTrace();
						/*L'unico caso valido per cui questo non funziona è se nei millisecondi che passano da quando capisco che quanti
						 * nodi ci sono nella rete a quando cerco di connettermi l'host remoto cade.*/
						Utils.stampaLogga("Errore RMI remoto! l'host potrebbe non essere più disponibile");
					} catch (NotBoundException e) {
						e.printStackTrace();
						Utils.stampaLogga("Errore RMI remoto");
					}
            	}
            }
         }, 5*1000, 25000); //dopo 5 secondi, ogni 30 secondi
        
        
        EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					root = new RootFrame(peerId, listaFileLocali, listaHostsRemoti, listaFileRemoti, helperLocalFiles, helperRemoteHosts, helperRemoteFiles, listTransferHandlerR2L,listTransferHandlerL2R);
					root.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
        
        /*Inizializzo la GUI*/
        //root = new RootFrame(this.peerId, listaFileLocali, listaHostsRemoti, listaFileRemoti, helperLocalFiles, helperRemoteHosts, helperRemoteFiles, listTransferHandler);
        
        /*E la rendo visibile*/
        //root.setVisible(true);
        
        /*Faccio partire il Whatcher della directory scelta*/
        try {
			WatchDir wd = new WatchDir(Paths.get(nomeCartellaCondivisa),false, this);
			wd.processEvents();
		} catch (IOException e) {
			Utils.stampaLogga("Impossibile eseguire watcher per cartella " + nomeCartellaCondivisa);
			e.printStackTrace();
		}
    }
    
	/* *****************************************************************************************************************************/
	/* *****************************************************************************************************************************/
	/* *****************************************************************************************************************************/
    ListSelectionListener obtainListSelectionListenerLocali(){
    	return helperLocalFiles;
    }
    
    ListSelectionListener obtainListSelectionListenerHosts(){
    	return helperRemoteHosts;
    }

    /**
     * La classe estende {@link javax.swing.event.ListSelectionListener}
     * per reagire ai click sulla lista dei file locali
     * @author matte
     */
    private class ListenerHelperLocalFiles implements ListSelectionListener{
    	
    	@SuppressWarnings("unchecked")
		@Override
    	public void valueChanged(ListSelectionEvent e) {
    		
			boolean adjust = e.getValueIsAdjusting();
	        if (!adjust) {
	        	javax.swing.JList<File> cosa = (JList<File>) e.getSource();
	        	
	        	try{
	        		System.out.println("Click FILE LOCALI: " + cosa.getSelectedIndex() + " ovvero " + listaFileLocali[cosa.getSelectedIndex()]);
	        	} catch(Exception e2){
	        		Utils.stampaLogga("Errore selezione file locale");
	        	}
	        }
    	}
    }
    
    /**
     * La classe estende {@link javax.swing.event.ListSelectionListener}
     * per reagire ai click sulla lista degli host remoti
     * @author matte
     */
    private class ListenerHelperRemoteHosts implements ListSelectionListener{
    	
    	@SuppressWarnings("unchecked")
		@Override
    	public void valueChanged(ListSelectionEvent e) {
    		
			boolean adjust = e.getValueIsAdjusting();
	        if (!adjust) {
	        	javax.swing.JList<String> cosa = (JList<String>) e.getSource();
	        	
	        	if(cosa.getSelectedIndex()<0){
	        		Utils.stampaLogga("Warn: click vuoto nel pannello host remoti");
	        		return;
	        	}
	        	
	        	/*Determino cosa ho cliccato*/
	        	String chi = listaHostsRemoti[cosa.getSelectedIndex()];
	        	
	        	Utils.stampaLogga("Click HOSTS REMOTI: " + cosa.getSelectedIndex() + " ovvero " + chi);
	        	currentRemoteHostClicked = cosa.getSelectedIndex();
	        	
	        	/*Devo invalidare la selezione perchè ho cambiato host!*/
	        	currentRemoteFileClicked = -1;
	        	
	        	try {
	        		
	        		/*Tento connessione con l'host remoto*/
	        		remote = (MyInterface) Naming.lookup(chi);
	        		
	        		/*Prendo l'elenco dei file dall'host remoto*/
					ArrayList<String> tmp = remote.getElencoFiles();
					listaFileRemoti = tmp.toArray(new String[tmp.size()]);
				
					/*Aggiorno la lista dei file remoti relativi al nodo cliccato*/
					//listTransferHandlerR2L = new ListTransferHandler(remote, peerId,true);
					//root.setTranferHandlerR2L(listTransferHandlerR2);
					
					//a volere essere proprio precisi si può salvare un'istanza statica di questi...invece che crearli ogni volta
					root.setTranferHandlerR2L(new ListTransferHandler(remote, peerId, true));
					root.setTranferHandlerL2R(new ListTransferHandler(remote, peerId, false));
					root.refreshFileRemoti(listaFileRemoti,-1);
					
				} catch (RemoteException | MalformedURLException | NotBoundException e1) {
					Utils.stampaLogga("Errore determinazione file remoti a seguito odi click su lista host remoti");
					e1.printStackTrace();
				}
	        }
    	}
    }
    
    /**
     * La classe estende {@link javax.swing.event.ListSelectionListener}
     * per reagire ai click sulla lista dei file remoti
     * @author matte
     */
    private class ListenerHelperRemoteFiles implements ListSelectionListener{

		@SuppressWarnings("unchecked")
		@Override
		public void valueChanged(ListSelectionEvent e) {
			boolean adjust = e.getValueIsAdjusting();
	        if (!adjust) {
	        	javax.swing.JList<String> cosa = (JList<String>) e.getSource();
	        	
	        	try{
		        	System.out.println("Click FILES REMOTI: " + cosa.getSelectedIndex() + " ovvero " + listaFileRemoti[cosa.getSelectedIndex()]);
		        	currentRemoteFileClicked = cosa.getSelectedIndex();
	        	} catch (Exception e2){
	        		Utils.stampaLogga("Errore click file remoti!");
	        	}
	        }
		}
    }
}