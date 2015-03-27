package com.theangi.graphics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;

import com.theangi.misc.Constants;
import com.theangi.misc.Utils;
import com.theangi.myinterfaces.MyInterface;

public class ListTransferHandler extends StringTransferHandler {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int[] indices = null;
    private int addIndex = -1; //Location where items were added
    private int addCount = 0;  //Number of items added.
            
    private MyInterface remote;
    private String nomeNodo;
    
    private boolean remote2Local;
    
    public ListTransferHandler(MyInterface remote, String nomeCartellaCondivisa, boolean R2L) {
    	this.remote = remote;
    	this.nomeNodo = nomeCartellaCondivisa;
    	this.remote2Local = R2L;
	}
    
    //Bundle up the selected items in the list
    //as a single string, for export.
    protected String exportString(JComponent c) {
        JList list = (JList)c;
        indices = list.getSelectedIndices();
        Object[] values = list.getSelectedValues();
        
        StringBuffer buff = new StringBuffer();

        for (int i = 0; i < values.length; i++) {
            Object val = values[i];
            buff.append(val == null ? "" : val.toString());
            if (i != values.length - 1) {
                buff.append("\n");
            }
        }
        
        return buff.toString();
    }

    //Take the incoming string and wherever there is a
    //newline, break it into a separate item in the list.
    protected void importString(JComponent c, String str) {
        JList target = (JList)c;
        
        Utils.stampaLogga("Voglio fare qualcosa con " + str + " sulla lista " + c.getName());
        
        /*Se voglio fare DOWNLOAD*/
        if(remote2Local && c.getName() == "jListLocalFiles" && !str.startsWith("database")){
        	
        	Utils.stampaLogga("Di preciso voglio DOWNLOAD del file remoto " + str + " nel file locale " + Utils.getFileInSharedFolder(new File(str), nomeNodo));
        	
        	if(remote == null){
        		Utils.stampaLogga("Errore! remote è nullo per download");
        	} else {
        		try {
					Utils.stampaLogga("Contatto " + remote.whoAreYou() + " per scaricare il file");
					
					if(remote.isFilePresent(str)){
						Utils.stampaLogga("Confermo: l'host remoto ha il file richiesto!");
						
						download(remote, new File(str), Utils.getFileInSharedFolder(new File(str), nomeNodo));
			        	Utils.stampaLogga("FATTO!!!! " + str + " scaricato con successo");
						
					} else {
						Utils.stampaLogga("Errore! L'host remoto NON ha il file richiesto!");
						return;
					}
					
				} catch (RemoteException e) {
					Utils.stampaLogga("Impossibile contattare host remoto per scaricare il file " + str);
					e.printStackTrace();
					return;
				}
        	}
        }
        else if (!remote2Local && c.getName()=="jListRemoteFiles"){
        	
        	Utils.stampaLogga("Di preciso voglio UPLOAD del file locale " + str);
        	
        	if(remote==null){
        		Utils.stampaLogga("Errore! remote è nullo");
        	} else {
        		try {
    				String rem = str.substring(str.lastIndexOf("/") + 1);
    				
    				Utils.stampaLogga("Contatto " + remote.whoAreYou() + " per UPLODARGLI il file " + rem);
    				Utils.upload(remote,new File(str), new File(rem));
    				
    			} catch (RemoteException e) {
    				Utils.stampaLogga("Impossibile contattare host remoto per caricare il file " + str);
    				e.printStackTrace();
    				return;
    			} catch (IOException e) {
    				e.printStackTrace();
    			}	
        	}
        }
        
        DefaultListModel listModel = (DefaultListModel)target.getModel();
        int index = target.getSelectedIndex();

        //Prevent the user from dropping data back on itself.
        //For example, if the user is moving items #4,#5,#6 and #7 and
        //attempts to insert the items after item #5, this would
        //be problematic when removing the original items.
        //So this is not allowed.
        if (indices != null && index >= indices[0] - 1 &&
              index <= indices[indices.length - 1]) {
            indices = null;
            return;
        }

        int max = listModel.getSize();
        if (index < 0) {
            index = max;
        } else {
            index++;
            if (index > max) {
                index = max;
            }
        }
        addIndex = index;
        String[] values = str.split("\n");
        addCount = values.length;
        for (int i = 0; i < values.length; i++) {
            listModel.add(index++, values[i]);
        }
    }

    //If the remove argument is true, the drop has been
    //successful and it's time to remove the selected items 
    //from the list. If the remove argument is false, it
    //was a Copy operation and the original list is left
    //intact.
    protected void cleanup(JComponent c, boolean remove) {
        if (remove && indices != null) {
            JList source = (JList)c;
            DefaultListModel model  = (DefaultListModel)source.getModel();
            //If we are moving items around in the same list, we
            //need to adjust the indices accordingly, since those
            //after the insertion point have moved.
            if (addCount > 0) {
                for (int i = 0; i < indices.length; i++) {
                    if (indices[i] > addIndex) {
                        indices[i] += addCount;
                    }
                }
            }
            
            for (int i = indices.length - 1; i >= 0; i--) {
                model.remove(indices[i]);
            }
        }
        indices = null;
        addCount = 0;
        addIndex = -1;
    }
    
    /**
     * Scarica dal server remoto il file src (remoto) nel file dest locale (lo crea)
     * @param server il server remoto da cui scaricare il file
     * @param src il percorso del file remoto
     * @param dest il percorso del file locale
     * @throws IOException
     */
    private void download(MyInterface server, File src, File dest){
        
    	if(server==null){
    		Utils.stampaLogga("Impossibile contattare host remoto");
    		return;
    	}
    	
    	long inizio = System.currentTimeMillis();
    	
    	try {
    		
    		/*Controllo di avere dove salvare il file che voglio salvare!*/
    		File f = new File(Constants.PREFIX_DATABASE + this.nomeNodo);
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
    
}
