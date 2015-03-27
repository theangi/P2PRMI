package com.theangi.myinterfaces;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * L'interfaccia che dichiara quali sono i metodi che sono disponibili all'esterno
 * @author matte
 *
 */
public interface MyInterface extends Remote {
	
	/**
	 * Torna un messaggio di saluto + nome dell'host
	 * 
	 * @return
	 * @throws RemoteException
	 */
    public String sayHello() throws RemoteException;
    
    /**
     * Ritorna il nome dell'host
     * @return
     * @throws RemoteException
     */
    public String whoAreYou() throws RemoteException;
    
    /**
     * Ritorna un java.io.OutputStream dato un oggetto astratto File.
     * Il metodo è chiamato da un sistema remoto che non sa quale sia la mia cartella condivisa.
     * Il metodo quindi aggiunge il prefisso al file per inserirlo nella cartella condivisa
     * @see {@link Utils}
     * 
     * @param f il file.
     * @return l'outputStream corrispondente.
     * @throws RemoteException se c'è un problema nella comunicazione RMI.
     * @throws FileNotFoundException se il file non esiste.
     * @throws IOException se c'è un problema di I/O.
     */
    public OutputStream getOutputStream(File f) throws RemoteException, FileNotFoundException, IOException;
    
    /**
     * Ritorna un java.io.InputStream relativo all'oggetto astratto File dato.
     * Il metodo è chiamato da un sistema remoto che non sa quale sia la mia cartella condivisa.
     * Il metodo quindi aggiunge il prefisso al file per inserirlo nella cartella condivisa
     * @see {@link Utils}
     * 
     * @param f il file.
     * @return l'InputStream corrispondente.
     * @throws RemoteException se c'è un problema nella comunicazione RMI.
     * @throws FileNotFoundException se il file non esiste.
     * @throws IOException se c'è un problema di I/O.
     */
    public InputStream getInputStream(File f) throws RemoteException, FileNotFoundException, IOException;
    
    /**
     * Ritorna un ArrayList di stringhe (quindi un oggetto serializzabile) con l'elenco di tutti i file
     * presenti sul nodo
     * @return i nomi dei file presenti su questo nodo.
     * @throws RemoteException
     */
    public ArrayList<String> getElencoFiles() throws RemoteException;
    
    
    /**
     * Determina se il file indicato è presente su questo host
     * @param f
     * @return
     * @throws RemoteException
     */
    public boolean isFilePresent(File f) throws RemoteException;
    
    public boolean isFilePresent(String s) throws RemoteException;
}