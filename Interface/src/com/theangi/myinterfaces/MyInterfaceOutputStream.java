package com.theangi.myinterfaces;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MyInterfaceOutputStream extends Remote {
 
	/**
	 * Scrive il byte. Il metodo è richiesto da java. Ignora i 24 bit rimanenti.
	 * @param b il byte da scrivere
	 * @throws IOException
	 * @throws RemoteException
	 */
    public void write(int b) throws IOException, RemoteException;
    
    /**
     * Scrive <code>len</code> byte a partire dall'offset <code>off</code> del buffer <code>buffer</code>
     * @param buffer
     * @param offset
     * @param lunghezza
     * @throws IOException
     * @throws RemoteException
     */
    public void write(byte[] buffer, int offset, int lunghezza) throws IOException, RemoteException;
    
    /**
     * Chiude lo stream
     * @throws IOException
     * @throws RemoteException
     */
    public void close() throws IOException, RemoteException;

}
