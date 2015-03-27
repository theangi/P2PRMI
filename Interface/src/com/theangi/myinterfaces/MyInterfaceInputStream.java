package com.theangi.myinterfaces;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MyInterfaceInputStream extends Remote {
    
	/**
	 * Legge <code>lunghezza</code> byte
	 * @param lunghezza il numero di byte da leggere
	 * @return i byte letti
	 * @throws IOException
	 * @throws RemoteException
	 */
    public byte[] leggiBytes(int lunghezza) throws IOException, RemoteException;
    
    /**
     * Legge UN byte dallo stream
     * @return
     * @throws IOException
     * @throws RemoteException
     */
    public int read() throws IOException, RemoteException;
    
    /**
     * Chiude lo stream
     * @throws IOException
     * @throws RemoteException
     */
    public void close() throws IOException, RemoteException;

}
