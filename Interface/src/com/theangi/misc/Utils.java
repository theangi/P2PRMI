package com.theangi.misc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.theangi.myinterfaces.MyInterface;

/**
 * Venendo al progetto, ho preso un pò confidenza con RMI e penso di aver capito 
 * tecnologicamente cosa sia possibile fare. A seguito di quello che mi aveva accennato, 
 * tra le opzioni mi piacerebbe sviluppare il "file system" distribuito P2P, quindi realizzare 
 * un'applicazione per la condivisione di file all'interno di un network.
 * Tra le numerose possibili realizzazioni, mi piacerebbe sviluppare un modello P2P "puro", 
 * senza alcun tipo di controllo centralizzato.
 * 
 * Tramite una apposita GUI (utilizzando Swing), ogni peer potrà principalmente:
    	Ottenere la lista dei file di un nodo.
    	Ottenere un file dato il suo nome.
    	Possibilità di caricare un file su un nodo.
    	Ottenere qualche statistica su file di un nodo, es: quante volte è stato richiesto.
 * 
 * Definite queste macro-funzionalità, nel dettaglio l'applicazione potrà:
    	Permettere di condividere in rete una directory scelta dall'utente
    	Sincronizzare una directory con una seconda remota
    	Realizzare un trasferimento a "chunks". Ci sono delle restrizioni in RMI che limitano lo scambio di file di grossa dimensione. L'idea è quella di poter trasferire un file generico di qualunque dimensione, quindi eseguire una specie di streaming dei dati.
 * 
 * @author matte
 */
public class Utils {

	/**
	 * Il metodo stampa in console il messaggio fornito
	 * @param cosa
	 */
	public static void stampa(String cosa){
		System.out.println(cosa);
	}
	
	/**
	 * Il metodo stampa e scrive nel file di log
	 * @param cosa
	 */
	public static void stampaLogga(String cosa){
		
		System.out.println(getCurrentTimeStamp() + cosa);
		
		PrintWriter out = null;
		
		try {
			/*L'ultimo true aggiunge in append o crea se non esiste*/
		    out = new PrintWriter(new BufferedWriter(new FileWriter("log.mytxt", true)));
		    
		    /*Ogni riga e poi un a capo*/
		    out.println(getCurrentTimeStamp() + cosa);
		    
		} catch (IOException e) {
		    System.out.println("Impossibile scrivere sul file di logging!");
		} finally {
			out.close();
		}
	}
	
	private static String getCurrentTimeStamp() {
	    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
	    Date now = new Date();
	    String strDate = sdfDate.format(now);
	    return strDate + " ";
	}
	
	/**
	 * Dato il singolo nome del file, ad esempio "pippo.txt"
	 * ritorna il suo percorso nella cartella condivisa, ad esempio "database/pippo.txt"
	 * @return il percorso relativo alla cartella condivisa
	 */
	public static File getFileInSharedFolder(File f, String nomePeer){
		
		//System.out.println("Sto per tornare il file " + Constants.PREFIX_DATABASE + nomePeer + File.separator + f.getPath());
		return new File(Constants.PREFIX_DATABASE + nomePeer + File.separator + f.getPath());
	}
	
	/**
	 * Dato il singolo nome del file, ad esempio "pippo.txt"
	 * ritorna il suo percorso nella cartella condivisa, ad esempio "database/pippo.txt"
	 * @return il percorso relativo alla cartella condivisa
	 */
	public static File getFileInSharedFolder(String s, String nomePeer){
		
		File f = new File(s);
		//System.out.println("Sto per tornare il file " + Constants.PREFIX_DATABASE + nomePeer + File.separator + f.getPath());
		return new File(Constants.PREFIX_DATABASE + nomePeer + File.separator + f.getPath());
	}
	
	/**
	 * TODO: da fare...se vogliamo la ricorsione all'interno della cartella condivisa
	 * @param name
	 * @param file
	 */
	public static void searchFileInSharedFolder(String name, File file){
	
		File[] list = file.listFiles();
        if(list!=null){
	        for (File fil : list){
	        	
	            if (fil.isDirectory()){
	            	searchFileInSharedFolder(name,fil);
	            } else if (name.equalsIgnoreCase(fil.getName())){
	                //System.out.println(fil.getParentFile());
	            }
	        }
        }
	}
	
    /**
     * Semplice metodo per sincronizzare la cartella locale con una remota
     * @param nomePeer
     * @param remote
     */
    static void syncFolder(String nomePeer, MyInterface remote){
    	
    	try {
			ArrayList<String> elencoFileRemoti = remote.getElencoFiles();
			
			for (String fr : elencoFileRemoti) {
				if(remote.isFilePresent(fr)){
					/*Scarico tutto*/
					Utils.download(remote, new File(fr), Utils.getFileInSharedFolder(fr, nomePeer), nomePeer);
				}
			}
			
			/*Ora carico tutto*/
			File[] listaFileLocali = new File(Constants.PREFIX_DATABASE + nomePeer).listFiles();
			for (File file : listaFileLocali) {
				Utils.upload(remote, file, new File(file.getName()));
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    /**
     * Scarica dal server remoto il file src (remoto) nel file dest locale (lo crea)
     * @param server il server remoto da cui scaricare il file
     * @param src il percorso del file remoto
     * @param dest il percorso del file locale
     * @throws IOException
     */
    public static void download(MyInterface server, File src, File dest, String nomeNodo){
        
    	if(server==null){
    		Utils.stampaLogga("Impossibile contattare host remoto");
    		return;
    	}
    	
    	long inizio = System.currentTimeMillis();
    	
    	try {
    		
    		/*Controllo di avere dove salvare il file che voglio salvare!*/
    		File f = new File(Constants.PREFIX_DATABASE + nomeNodo);
    		if(!f.exists()){
    			System.out.println("Ho dovuto creare io la cartella database!");
    			f.mkdirs();
    		}
    		
			Utils.copy (server.getInputStream(src), new FileOutputStream(dest));
			
		} catch (RemoteException e) {
			System.out.println("Eccezione remota. Impossibile scaricare il file " + src.getPath());
			e.printStackTrace();
			return;
		} catch (FileNotFoundException e) {
			System.out.println("File non trovato!!!");
			e.printStackTrace();
			return;
		} catch (IOException e) {
			System.out.println("Errore IO");
			e.printStackTrace();
			return;
		}
    	
		long fine = System.currentTimeMillis();
		long quanto = (fine-inizio);
		
		try {
			System.out.print("Download eseguito in " + quanto + "ms ");
			System.out.print("Ovvero " + quanto/1000 + " secondi");
			System.out.println("");
			
		} catch (Exception e) {
			//Localmente impiega così poco tempo che devo gestire eccezioni
		}
		
		try {
			System.out.print("Quindi in media " + src.length()/(quanto/1000) + "B/s");
			System.out.print("Ovvero " + (src.length()/1024) / (quanto/1000) + "KB/s");
			System.out.println("");
		} catch (Exception e){
			//Localmente impiega così poco tempo che devoi gestire eccezioni
		}
    }
    
    public static void upload(MyInterface server, File src, File dest) throws IOException {
        copy (new FileInputStream(src), server.getOutputStream(dest));
        
//      String dacaricare ="immagine.jpg";
//      upload(remote, Utils.getFileInSharedFolder(dacaricare), new File(dacaricare));
    }
    
    public static void copy(InputStream in, OutputStream out) throws IOException {
        
    	System.out.println("Scrivo su disco l'inputstream in un outputstream!");
        
    	byte[] b = new byte[Constants.BUF_SIZE];
        int len;
        while ((len = in.read(b)) >= 0) {
            out.write(b, 0, len);
        }
        
        /*Chiudo lo stream*/
        in.close();
        out.close();
    }
}
