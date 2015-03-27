package com.theangi.mystreams;

import java.io.IOException;
import java.io.OutputStream;
import java.rmi.server.UnicastRemoteObject;

import com.theangi.misc.Constants;
import com.theangi.myinterfaces.MyInterfaceOutputStream;

/**
 * La classe è un wrapper della classe java.io.OutputStream.
 * Implementa inoltre l'interfaccia REMOTA per l'outputStream
 * @author matte
 */
public class WrapperOutputStream implements MyInterfaceOutputStream {

	/**L'OutputStream di java (classe astratta)*/
    private OutputStream out;
    
    /**
     * Costruttore. esporta l'oggetto sulla porta predefinita.
     * @param out
     * @throws IOException
     */
    public WrapperOutputStream(OutputStream out) throws IOException {
        this.out = out;
        
        /*Esporto me stesso sulla porta predefinita*/
        UnicastRemoteObject.exportObject(this, Constants.RMI_PORT);
    }
    
    public void write(int b) throws IOException {
        out.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    public void close() throws IOException {
        out.close();
    }

}
