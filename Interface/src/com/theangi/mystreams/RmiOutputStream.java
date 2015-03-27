package com.theangi.mystreams;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import com.theangi.myinterfaces.MyInterfaceOutputStream;

/**
 * La classe è un OutputStream (estende java.io.InputStream) serializzabile 
 * @author Matteo Angiari
 *
 */
public class RmiOutputStream extends OutputStream implements Serializable {

	private static final long serialVersionUID = 1L;

	private MyInterfaceOutputStream out;

	public RmiOutputStream(WrapperOutputStream out) {
		this.out = out;
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
