package com.theangi.graphics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DropMode;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileSystemView;

import com.theangi.misc.Utils;

public class RootFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	final String nomeNodo;
	
	final File[] localFiles;
	String[] remoteHosts;
	String[] remoteFiles;
	
	final ListSelectionListener ListenerFileLocali;
	final ListSelectionListener listenerHostRemoti;
	final ListSelectionListener listenerFileRemoti;

	private ListTransferHandler listTransferHandler_R2L;
	private ListTransferHandler listTransferHandler_L2R;
	
	private JScrollPane scrollPaneFileLocali;
	private JScrollPane scrollPaneHostsRemoti;
	private JScrollPane scrollPaneFileRemoti;
	
	private JList<File> jListLocalFiles;
	private JList<String> jListRemoteHosts;
	private JList<String> jListRemoteFiles;
	
	/**
	 * 
	 * @param nomeNodo il nome del nodo
	 * @param localFiles l'elenco dei file locali
	 */
	public RootFrame(String nomeNodo, 
			File[] localFiles, 
			String[] listaHostRemoti, 
			String[] remoteFiles, 
			ListSelectionListener listenerFileLocali, 
			ListSelectionListener listenerHostRemoti, 
			ListSelectionListener listenerFileRemoti,
			ListTransferHandler listTransferHandlerDown,
			ListTransferHandler listTransferHandlerUp) {
		
		/*Prendo parametri fondamentali*/
		this.nomeNodo = nomeNodo;
		
		this.localFiles = localFiles;
		this.remoteHosts = listaHostRemoti;
		this.remoteFiles = remoteFiles;
		
		this.ListenerFileLocali = listenerFileLocali;
		this.listenerHostRemoti = listenerHostRemoti;
		this.listenerFileRemoti = listenerFileRemoti;
		
		this.listTransferHandler_R2L = listTransferHandlerDown;
		this.listTransferHandler_L2R = listTransferHandlerUp;
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// I primi due dicono di farlo parire non in alto a sx nello schermo. Gli ultimi due la dimensione
		setBounds(100, 100, 640, 480);
        setTitle("P2P with RMI");
        setMinimumSize(new Dimension(640, 480));
        
        JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		buildGUI();
		
       }
	
	void buildGUI(){
		
		/*Il pannello principale*/
        JPanel main = new JPanel();
        
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        
        	/*Label in alto*/
	        JLabel a = new JLabel("Peer: " + nomeNodo);
	        a.setAlignmentX(Component.CENTER_ALIGNMENT);
	        a.setBorder(new EmptyBorder(10, 10, 10, 10));
	        a.setFont(new Font(Font.SANS_SERIF, 3, 25));
	        
	        /*Pannello diviso in due, MyFile e RemoteHosts*/
	        JPanel b = new JPanel();
	        b.setBorder(new EmptyBorder(10, 10, 10, 10));
	        b.setAlignmentX(Component.CENTER_ALIGNMENT);
	        b.setLayout(new GridLayout(1,2));
	        
	        	/*Pannello dei miei files*/
		        JPanel c = new JPanel();
		        
		        /*Creo un bordo con titolo, e padding di 10px*/
		        TitledBorder tbc = BorderFactory.createTitledBorder("MyFiles");
		        tbc.setTitleJustification(TitledBorder.CENTER);
			    c.setBorder(tbc);
			    c.setLayout(new BorderLayout());
			    
			    /* *********************************************************** */
			    	/*L'elenco dei file*/
			    	scrollPaneFileLocali = new JScrollPane();
			    		/*I files in una lista*/
				    	jListLocalFiles = new JList<File>(localFiles);
				    	jListLocalFiles.setName("jListLocalFiles");
				        jListLocalFiles.setCellRenderer(new FileRenderer());
				        jListLocalFiles.addListSelectionListener(ListenerFileLocali);
				        jListLocalFiles.addMouseListener(new MyMouseListener(jListLocalFiles));
				        jListLocalFiles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				        
				        if(localFiles!=null)
				        	jListLocalFiles.setSelectedIndex(-1);	//evita che di default venga segnato qualcosa
				        
				        jListLocalFiles.setDragEnabled(true);
				        jListLocalFiles.setDropMode(DropMode.INSERT);
				        jListLocalFiles.setTransferHandler(listTransferHandler_R2L);
				        
			        scrollPaneFileLocali.setViewportView(jListLocalFiles);
			        scrollPaneFileLocali.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	
			    /*Lo aggiungo centrato, così si ridimensiona grande come il pannello che lo contiene*/
	            c.add(scrollPaneFileLocali, SwingConstants.CENTER);
                
	            /* *********************************************************** */
	            
	            /*Pannello degli host remoti*/
		        JPanel d = new JPanel();
		        TitledBorder tbd = BorderFactory.createTitledBorder("Hosts");
		        tbd.setTitleJustification(TitledBorder.CENTER);
			    d.setBorder(tbd);
			    d.setLayout(new GridLayout(2,1));
			    	
			    	/* *********************************************************** */
			    	/*Il primo pannello è una lista di host remoti*/
			    	scrollPaneHostsRemoti = new JScrollPane();
				    	
			    		/*Gli hosts in una lista*/
				    	jListRemoteHosts = new JList<String>(remoteHosts);
						jListRemoteHosts.setCellRenderer(new HostRenderer(remoteHosts));
						jListRemoteHosts.addListSelectionListener(listenerHostRemoti);
						jListRemoteHosts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						
						try{
							jListRemoteHosts.setSelectedIndex(-1);	//evita che di default venga segnato qualcosa
						}catch(Exception e){
							//niente
						}
				    	
				    /* *********************************************************** */
			    	/*Il secondo è un pannello con i file remoti relativi*/
			    	scrollPaneFileRemoti = new JScrollPane();
			    		
			    		/*I files in una lista*/
				    	jListRemoteFiles = new JList<String>(remoteFiles);
				    	jListRemoteFiles.setName("jListRemoteFiles");
				        jListRemoteFiles.setCellRenderer(new StringRenderer(remoteFiles));
				        jListRemoteFiles.addListSelectionListener(listenerFileRemoti);
				        jListRemoteFiles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				        jListRemoteFiles.setTransferHandler(listTransferHandler_L2R);
				        jListRemoteFiles.setDragEnabled(true);
				        jListRemoteFiles.setDropMode(DropMode.INSERT);
				        
				        try{
				        	jListRemoteFiles.setSelectedIndex(-1);	//evita che di default venga segnato qualcosa
				        }catch(Exception e){
				        	//niente
				        }
				        
			        scrollPaneFileRemoti.setVisible(false);
			    
		    	d.add(scrollPaneHostsRemoti);
			    d.add(scrollPaneFileRemoti);

			b.add(c,BorderLayout.LINE_START);
			b.add(d,BorderLayout.LINE_END);
		        
        main.add(a, BorderLayout.PAGE_START);
        main.add(b, BorderLayout.CENTER);
        
        add(main);
	}
	
	public void refreshFileLocali(File[] files, int lastSelected){
		
		if(files==null){
			Utils.stampaLogga("Errore: files è nullo quando dovrebbe essere un vettore vuoto");
		} else {
			Utils.stampaLogga("Aggiorno elenco file locali: " + files.length + " file trovati");
		}

		jListLocalFiles.removeAll();
		jListLocalFiles.setListData(files);
		jListLocalFiles.setCellRenderer(new FileRenderer());
		//jListRemoteFiles = new JList(files);
		//jListRemoteFiles.setCellRenderer(new FileRendererList(files.));
		//jListRemoteFiles.addListSelectionListener(listenerHostRemoti);
		//jListRemoteFiles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		if(lastSelected >=0 & lastSelected < files.length){
			jListLocalFiles.setSelectedIndex(lastSelected);
			Utils.stampaLogga("L'ultima volta era selezionato il " + lastSelected);
		}

		scrollPaneFileLocali.setViewportView(jListLocalFiles);
		scrollPaneFileLocali.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	}
	
	public void refreshHostRemoti(String[] hosts, int lastSelected){
		
		if(hosts==null){
			Utils.stampaLogga("Errore: hosts è nullo quando dovrebbe essere un vettore vuoto");
		}
		
		this.remoteHosts = hosts;

		jListRemoteHosts.setListData(hosts);
		jListRemoteHosts.setCellRenderer(new HostRenderer(hosts));

		if(lastSelected >=0 & lastSelected < hosts.length){
			jListRemoteHosts.setSelectedIndex(lastSelected);
			Utils.stampaLogga("host remoti, L'ultima volta era selezionato il " + lastSelected);
		}

		scrollPaneHostsRemoti.setViewportView(jListRemoteHosts);
		scrollPaneHostsRemoti.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	}
	
	public void refreshFileRemoti(String[] files, int lastSelected){
		
		if(files==null){
			Utils.stampaLogga("Errore: files è nullo quando dovrebbe essere un vettore vuoto");
		}
		
		this.remoteFiles = files;

		jListRemoteFiles.setListData(files);
		jListRemoteFiles.setCellRenderer(new StringRenderer(files));
		//jListRemoteFiles = new JList(files);
		//jListRemoteFiles.setCellRenderer(new FileRendererList(files.));
		//jListRemoteFiles.addListSelectionListener(listenerHostRemoti);
		//jListRemoteFiles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		if(lastSelected >=0 & lastSelected < files.length){
			jListRemoteFiles.setSelectedIndex(lastSelected);
			Utils.stampaLogga("L'ultima volta era selezionato il " + lastSelected);
		}

		scrollPaneFileRemoti.setViewportView(jListRemoteFiles);
		scrollPaneFileRemoti.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		scrollPaneFileRemoti.setVisible(true);
	}
	
	public void setTranferHandlerR2L(ListTransferHandler l){
		Utils.stampa("Cambiato tranfert handler Remote2Local!");
		this.listTransferHandler_R2L = l;
		jListLocalFiles.setTransferHandler(listTransferHandler_R2L);
	}
	
	public void setTranferHandlerL2R(ListTransferHandler l){
		Utils.stampa("Cambiato tranfert handler Local2Remote!");
		this.listTransferHandler_L2R = l;
		jListRemoteFiles.setTransferHandler(listTransferHandler_L2R);
	}
}

class FileRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 1L;

    @SuppressWarnings("rawtypes")
	@Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    	
        Component c = super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
        JLabel l = (JLabel) c;
        File f = (File)value;
        l.setText(f.getName());
        
        /*è un file, quindi ha una icona corrispondente*/
        l.setIcon(FileSystemView.getFileSystemView().getSystemIcon(f));
        return l;
    }
}

/** La classe definisce come è una riga della lista di file remoto*/
class StringRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 1L;

	private String[] listaRemoti;
	
	public StringRenderer(String[] lista) {
		this.listaRemoti = lista;
	}
	
   @SuppressWarnings("rawtypes")
   @Override
   public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

       Component c = super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
       JLabel l = (JLabel) c;
       if(index >=0)
    	   l.setText(listaRemoti[index]);
       
       return l;
   }
}

class HostRenderer extends DefaultListCellRenderer{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String[] hosts;
	
	public HostRenderer(String[] hosts) {
		super();
		this.hosts = hosts;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

		Component c = super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
		JLabel l = (JLabel) c;
		if(index>=0 && index < hosts.length){
			l.setText(hosts[index]);
			l.setForeground(Color.MAGENTA);
		}
		return l;
	}
}

/**
 * Piccola classe
 * @author matte
 *
 */
class MyMouseListener implements MouseListener{

	JList<File> lista;
	JPopupMenu jpm;
	
	JMenuItem jMenuElimina = new JMenuItem("Elimina");
	
	public MyMouseListener(final JList<File> lista) {
		this.lista = lista;
		jpm = new JPopupMenu();
		jpm.add(jMenuElimina);
		jMenuElimina.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				File cosa = lista.getSelectedValue();
				
				if(cosa.exists()){
					System.out.println("Ora si! voglio eliminare: " + cosa);
					cosa.delete();
				}
			}
		});
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		check(e);
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		check(e);
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public void check(MouseEvent e) {
	    if (e.isPopupTrigger()) { //if the event shows the menu
	        lista.setSelectedIndex(lista.locationToIndex(e.getPoint())); //select the item
	        jpm.show(lista, e.getX(), e.getY()); //and show the menu
	        Utils.stampa("Eccomi qua");
	    }
	}
	
}