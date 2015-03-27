package com.theangi.mystreams;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import com.theangi.misc.Constants;
import com.theangi.myinterfaces.MyInterfaceInputStream;

/**
 * La classe è un wrapper della classe java.io.InputStream.
 * Implementa inoltre l'interfaccia REMOTA per l'inputStream
 * @author matte
 *
 */
public class WrapperInputStream implements MyInterfaceInputStream {

	/** L'inputStream di java (classe astratta) */
    private InputStream in;
    
    /** Il buffer che contiene i dati*/
    private byte[] buffer;

    /**
     * Costruttore. Esporta l'oggetto sulla porta predefinita.
     * @param il java.io.InputStream
     * @throws IOException
     */
    public WrapperInputStream(InputStream in) throws IOException {
        this.in = in;
        UnicastRemoteObject.exportObject(this, Constants.RMI_PORT);
    }

    public void close() throws IOException, RemoteException {
        in.close();
    }

    public int read() throws IOException, RemoteException {
        return in.read();
    }
    
    public byte[] leggiBytes(int lunghezza) throws IOException, RemoteException {
        
    	if (buffer == null || buffer.length != lunghezza)
            buffer = new byte[lunghezza];
            
    	/*Leggo i lunghezza byte dall'inputstream e li memorizzo in buffer*/
        int letti = in.read(buffer);
        
        /*in.read() ritorna -1 se ha raggiunto la fine del file*/
        if (letti < 0)
            return null;
        
        /*Se per qualche motivo ho letto (meno) byte di quelli richiesti, torno quelli*/
        if (letti != lunghezza) {
        	
            byte[] tmp = new byte[letti];
            
            /*Esegue la copia*/
            System.arraycopy(buffer, 0, tmp, 0, letti);
            
            return tmp;
        } else {
        	
        	/*Ritorno il buffer*/
        	return buffer;
        }
    }
}