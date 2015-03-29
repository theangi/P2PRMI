package com.theangi.misc;

public class Constants {

	/**La porta di default su cui opera rmi: 1099*/
	public static final int RMI_PORT = 1099;
	
	/** La stringa "database" */
	public static final String PREFIX_DATABASE = "database_";
	
	/** La stringa "nodo" */
	public static final String PREFIX_PEER = "nodo";
	
	/** La dimensione di invio di ogni chunk per i file*/
	public static final int BUF_SIZE = 1024 * 64;
	
	/** Timeout oltre il quale un host viene considerato irraggiungibile*/
	public static final int TIMEOUT_SEARCH_HOSTS = 30000;
	
}
