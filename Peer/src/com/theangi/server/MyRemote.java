package com.theangi.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.AccessException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import com.theangi.misc.Constants;
import com.theangi.misc.Utils;
import com.theangi.myinterfaces.MyInterface;
import com.theangi.mystreams.RmiInputStream;
import com.theangi.mystreams.RmiOutputStream;
import com.theangi.mystreams.WrapperInputStream;
import com.theangi.mystreams.WrapperOutputStream;

public class MyRemote extends UnicastRemoteObject implements MyInterface {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** Il nome di questo peer */
	private final String peerID;
	
	/** Il nome della cartella condivisa*/
	private final String nomeCartellaCondivisa;

	/** Il registry locale*/
	Registry rmiRegistry;


	/**
	 * Il Costruttore della parte remota
	 * @param id l'id di questo peer
	 * @throws RemoteException
	 */
    public MyRemote(String id) throws RemoteException {
        super();

        this.peerID = id;
        this.nomeCartellaCondivisa = Constants.PREFIX_DATABASE + this.peerID;

        ArrayList<String> nomiFiles = new ArrayList<String>();
        nomiFiles = getElencoFiles();

        Utils.stampaLogga("Vediamo che files ho a disposizione da condividere");
        if(nomiFiles == null){
        	Utils.stampaLogga("Nessun file a disposizione!");	
        } else {
        	
        	/*Li mostro*/
        	for (String string : nomiFiles) {
        		Utils.stampaLogga(string);
        	}
        }
    }

    /* ***************************************************************************************** */
    /* Da qua in poi metodi  INTERNI */
    /* ***************************************************************************************** */

    /**
     * Inizia l'esecuzione del lato server
     */
    public void start(){

    	Utils.stampaLogga("Avvio server RMI...");

		/*Creo ed esporto il mio rmiregistry*/
		try {
			
			/*Provo a creare il mio registry! Se questo programma viene eseguito
			 * una sola volta per singola macchina, non è un problema!*/
			rmiRegistry = LocateRegistry.createRegistry(Constants.RMI_PORT);
			
		} catch (RemoteException e) {
		
			/*Se l'eccezione è export*/
			if(e instanceof ExportException){
				
				/*Se sono qua dentro, significa sto tentando di eseguire due volte lo stesso server*/
				System.out.println("Sono il secondo nodo di test! Allora devo eseguire GETREGISTRY!!!!");
				
				/*Allora tento di prendere quello che già avevo funzionante*/
				try {
					rmiRegistry = LocateRegistry.getRegistry(Constants.RMI_PORT);
				} catch (RemoteException e1) {
					System.out.println("Peggio di così non può andare");
					e1.printStackTrace();
				}
			} else {
				Utils.stampaLogga("Grave errore remoto: impossibile creare registro");
				e.printStackTrace();	
			}
		}
		
		Utils.stampaLogga("Ecco il registry:" + rmiRegistry.toString());
		Utils.stampaLogga("Lato server, Eseguo bind() con nome " + this.peerID );

        try {
        	/*Tutti i miei servizi li esporto con il nome "nodox"*/
			rmiRegistry.rebind(this.peerID, this);
		} catch (AccessException e) {
			Utils.stampaLogga("Impossibile eseguire operazione. Permesso negato in creazione registry!");
			e.printStackTrace();
		} catch (RemoteException e) {
			Utils.stampaLogga("Impossibile creare registry!");
			e.printStackTrace();
		}
    }

    /**
     * termina l'esecuzione del lato server
     */
    public void stop(){
    	
    	Utils.stampaLogga("Terminazione server RMI...");
        
        try {

        	rmiRegistry.unbind(this.peerID);
			unexportObject(this, true);
			unexportObject(rmiRegistry, true);
			Utils.stampaLogga("Server terminato correttamente");
			
		} catch (NoSuchObjectException e) {
			System.out.println("L'oggetto non era esportato");
			e.printStackTrace();
		} catch (AccessException e) {
			System.out.println("Impossibile eseguire unbind");
			e.printStackTrace();
		} catch (RemoteException e) {
			System.out.println("Errore comunicazione RMI");
			e.printStackTrace();
		} catch (NotBoundException e) {
			System.out.println("Il nome corrente non era associato al registry");
			e.printStackTrace();
		}
    }

    /* ***************************************************************************************** */
    /* Da qua in poi metodi ESPORTATI*/
    /* ***************************************************************************************** */
    
    @Override
    public String sayHello() {
        return "Ciao! Grazie per avermi contattato. Io mi chiamo " + peerID;
    }

	@Override
	public OutputStream getOutputStream(File f) throws FileNotFoundException, IOException {
		
		/* OutputStream dell'API java non è serializzabile di suo.
		 * Occorre quindi ritornare un oggetto OutputStream ma serializzabile -> RmiOutputStream
		 * Serve però anche un wrapper che mantenga l'outputStream e il file "vero" FileOutputStream
		 */
		return new RmiOutputStream(new WrapperOutputStream(new FileOutputStream(Utils.getFileInSharedFolder(f, this.peerID))));
	}

	@Override
	public InputStream getInputStream(File f) throws FileNotFoundException, IOException {
		
		/* InputStream dell'API java non è serializzabile di suo.
		 * Occorre quindi ritornare un oggetto InputStream ma serializzabile -> RmiInputStream
		 * Serve però anche un wrapper che mantenga l'inputStream e il file "vero" FileInputStream
		 */
		return new RmiInputStream(new WrapperInputStream(new FileInputStream(Utils.getFileInSharedFolder(f, this.peerID))));
	}

	@Override
	public ArrayList<String> getElencoFiles(){
		
		ArrayList<String> nomiFile = new ArrayList<String>();

		/*Ottengo la lista di file locali della cartella condivisa*/
		File[] files = new File(nomeCartellaCondivisa).listFiles();
		
		/*Se avevo qualcosa*/
		if(files == null){
			Utils.stampaLogga("Impossibile determinare files presenti su questo host. Directory \"database\" assente?");
			return null;
		}
		
		/*Aggiungo alla lista l'elenco dei nomi*/
		for (File file : files) {
		    if (file.isFile()) {
		        nomiFile.add(file.getName());
		    }
		}
		
		/*E ritorno la lista*/
		return nomiFile;

	}

	@Override
	public boolean isFilePresent(String s) throws RemoteException {
		return isFilePresent(new File(s));
	}
	
	@Override
	public boolean isFilePresent(File f) throws RemoteException {
		
		/*Aggiungo il prefisso*/
		File f2 = new File(nomeCartellaCondivisa + File.separator + f.getPath());
		
		/*Vediamo se esiste*/
		return (f2.exists() && !f2.isDirectory());
	}

	@Override
	public String whoAreYou() throws RemoteException {
		return this.peerID;
	}
}