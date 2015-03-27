package com.theangi.mystreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import com.theangi.myinterfaces.MyInterfaceInputStream;

/**
 * La classe è un InputStream (estende java.io.InputStream) serializzabile 
 * @author Matteo Angiari
 *
 */
public class RmiInputStream extends InputStream implements Serializable {


	private static final long serialVersionUID = 1L;
	
	MyInterfaceInputStream in;

	public RmiInputStream(MyInterfaceInputStream in) {
		this.in = in;
	}

	public int read() throws IOException {
		return in.read();
	}

	public int read(byte[] b, int off, int len) throws IOException {
		byte[] b2 = in.leggiBytes(len);
		
		if (b2 == null)
			return -1;
		
		int i = b2.length;
		
		System.arraycopy(b2, 0, b, off, i);
		
		return i;
	}

	public void close() throws IOException {
		super.close();
	}
}