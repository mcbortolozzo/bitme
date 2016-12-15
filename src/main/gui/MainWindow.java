package main.gui;


import main.Client;
import main.torrent.TorrentFile;
import main.torrent.TorrentManager;
import main.torrent.file.TorrentFileInfo;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Written by
 * Ricardo Atanazio S Carvalho
 * Marcelo Cardoso Bortolozzo
 * Hajar Aahdi
 * Thibault Tourailles
 */
public class MainWindow {

	private JFrame frmBitme;
	private JTextField nbActiveTransfers;
	private JTable torrents;
    private JTable peersTable;
    private JSplitPane splitPanel;
	private final JFileChooser fc = new JFileChooser();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
        try {
            final Client c = new Client(Client.PORT);
			c.run();
            SwingUtilities.invokeLater(() -> {
                try {
                    MainWindow window = new MainWindow(c);
                    window.frmBitme.setVisible(true);
                    window.finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	/**
	 * Create the application.
	 */
	public MainWindow(Client c) {
		initialize(c);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize(Client c) {
		frmBitme = new JFrame();
		frmBitme.setTitle("BitMe");
		frmBitme.setBounds(100, 100, 450, 300);
		frmBitme.setMinimumSize(new Dimension(650, 500));
		frmBitme.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);


		/**********************************************************************
		 *                            MENU SETUP                              *
		 **********************************************************************/
		JMenuBar menuBar = new JMenuBar();
		frmBitme.setJMenuBar(menuBar);

		JMenu mnFichier = new JMenu("Fichier");
		menuBar.add(mnFichier);

		JMenuItem mntmCrerUnFichier = new JMenuItem("Créer un fichier torrent");
        mnFichier.addActionListener(e -> MainWindow.this.createTorrentFile());
		mnFichier.add(mntmCrerUnFichier);

		JMenuItem mntmOuvrirUnFichier = new JMenuItem("Ouvrir un fichier torrent");
		mntmOuvrirUnFichier.addActionListener(e -> MainWindow.this.openTorrentFile());
		mnFichier.add(mntmOuvrirUnFichier);

		JMenuItem mntmOuvrirUnLien = new JMenuItem("Ouvrir un lien magnet");
		mntmOuvrirUnLien.setEnabled(false);
		mnFichier.add(mntmOuvrirUnLien);

		JSeparator separator = new JSeparator();
		mnFichier.add(separator);

		JMenuItem mntmFermerLaFentre = new JMenuItem("Fermer la fenêtre");
		mntmFermerLaFentre.addActionListener(e -> System.exit(0));
		mnFichier.add(mntmFermerLaFentre);

		JMenu mndition = new JMenu("Édition");
		menuBar.add(mndition);

		JMenuItem mntmEffacer = new JMenuItem("Effacer");
		mntmEffacer.addActionListener(e -> {
            ((TorrentTableModel)torrents.getModel()).removeTorrent(torrents.getSelectedRows());
            updateActiveTorrents();
        });
		mndition.add(mntmEffacer);

		JMenuItem mntmToutSlectionner = new JMenuItem("Tout sélectionner");
		mntmToutSlectionner.addActionListener(e -> torrents.setRowSelectionInterval(0, torrents.getModel().getRowCount()));
		mndition.add(mntmToutSlectionner);

		JMenuItem mntmToutDslectionner = new JMenuItem("Tout désélectionner");
		mntmToutDslectionner.addActionListener(e -> {
            torrents.setRowSelectionInterval(0, 0);
            torrents.getSelectionModel().removeIndexInterval(0, torrents.getModel().getRowCount());
        });
		mndition.add(mntmToutDslectionner);

		JMenu mnTransferts = new JMenu("Transferts");
		menuBar.add(mnTransferts);

		JMenu mnFentre = new JMenu("Fenêtre");
		menuBar.add(mnFentre);
		SpringLayout springLayout = new SpringLayout();
		frmBitme.getContentPane().setLayout(springLayout);

        /**********************************************************************
         *                          PANELS SETUP                              *
         **********************************************************************/
		JPanel bottomPanel = new JPanel();
		springLayout.putConstraint(SpringLayout.WEST, bottomPanel, 0, SpringLayout.WEST, frmBitme.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, bottomPanel, 0, SpringLayout.SOUTH, frmBitme.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, bottomPanel, 0, SpringLayout.EAST, frmBitme.getContentPane());
		bottomPanel.setBackground(SystemColor.window);
		springLayout.putConstraint(SpringLayout.NORTH, bottomPanel, -40, SpringLayout.SOUTH, frmBitme.getContentPane());
		frmBitme.getContentPane().add(bottomPanel);

		splitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        springLayout.putConstraint(SpringLayout.WEST, splitPanel, 0, SpringLayout.WEST, bottomPanel);
        springLayout.putConstraint(SpringLayout.SOUTH, splitPanel, 0, SpringLayout.NORTH, bottomPanel);
        springLayout.putConstraint(SpringLayout.EAST, splitPanel, 0, SpringLayout.EAST, bottomPanel);
        frmBitme.getContentPane().add(splitPanel);

		JPanel mainPanel = new JPanel();
		mainPanel.setBackground(SystemColor.text);
		splitPanel.setTopComponent(mainPanel);

		JPanel buttonPanel = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, splitPanel, 45, SpringLayout.NORTH, buttonPanel);
		SpringLayout sl_mainPanel = new SpringLayout();
		mainPanel.setLayout(sl_mainPanel);

        JScrollPane scrollPane = new JScrollPane();
        sl_mainPanel.putConstraint(SpringLayout.NORTH, scrollPane, 0, SpringLayout.NORTH, mainPanel);
        sl_mainPanel.putConstraint(SpringLayout.WEST, scrollPane, 0, SpringLayout.WEST, mainPanel);
        sl_mainPanel.putConstraint(SpringLayout.SOUTH, scrollPane, 0, SpringLayout.SOUTH, mainPanel);
        sl_mainPanel.putConstraint(SpringLayout.EAST, scrollPane, 0, SpringLayout.EAST, mainPanel);
        mainPanel.add(scrollPane);

        JPanel infoPanel = new JPanel();
        splitPanel.setRightComponent(infoPanel);
        SpringLayout sl_infoPanel = new SpringLayout();
        infoPanel.setLayout(sl_infoPanel);

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        sl_infoPanel.putConstraint(SpringLayout.NORTH, tabbedPane, 0, SpringLayout.NORTH, infoPanel);
        sl_infoPanel.putConstraint(SpringLayout.WEST, tabbedPane, 0, SpringLayout.WEST, infoPanel);
        sl_infoPanel.putConstraint(SpringLayout.SOUTH, tabbedPane, 0, SpringLayout.SOUTH, infoPanel);
        sl_infoPanel.putConstraint(SpringLayout.EAST, tabbedPane, 0, SpringLayout.EAST, infoPanel);
        infoPanel.add(tabbedPane);

        JPanel peersPanel = new JPanel();
        tabbedPane.addTab("Peers", null, peersPanel, null);
        SpringLayout sl_peersPanel = new SpringLayout();
        peersPanel.setLayout(sl_peersPanel);

        JScrollPane peersScrollPane = new JScrollPane();
        sl_peersPanel.putConstraint(SpringLayout.NORTH, peersScrollPane, 0, SpringLayout.NORTH, peersPanel);
        sl_peersPanel.putConstraint(SpringLayout.WEST, peersScrollPane, 0, SpringLayout.WEST, peersPanel);
        sl_peersPanel.putConstraint(SpringLayout.SOUTH, peersScrollPane, 0, SpringLayout.SOUTH, peersPanel);
        sl_peersPanel.putConstraint(SpringLayout.EAST, peersScrollPane, 0, SpringLayout.EAST, peersPanel);
        peersPanel.add(peersScrollPane);

        peersTable = new JTable(new PeersTableModel(TorrentManager.getInstance()));
        peersScrollPane.setViewportView(peersTable);

        JPanel piecePanel = new JPanel();
        tabbedPane.addTab("Pieces", null, piecePanel, null);
        SpringLayout sl_piecePanel = new SpringLayout();
        piecePanel.setLayout(sl_piecePanel);

        JScrollPane pieceScrollPane = new JScrollPane();
        sl_piecePanel.putConstraint(SpringLayout.NORTH, pieceScrollPane, 0, SpringLayout.NORTH, piecePanel);
        sl_piecePanel.putConstraint(SpringLayout.WEST, pieceScrollPane, 0, SpringLayout.WEST, piecePanel);
        sl_piecePanel.putConstraint(SpringLayout.SOUTH, pieceScrollPane, 0, SpringLayout.SOUTH, piecePanel);
        sl_piecePanel.putConstraint(SpringLayout.EAST, pieceScrollPane, 0, SpringLayout.EAST, piecePanel);
        piecePanel.add(pieceScrollPane);

        JPanel piecesPanel = new PiecesPanel();
        pieceScrollPane.setViewportView(piecesPanel);

        /**********************************************************************
         *                       TORRENT TABLE SETUP                          *
         **********************************************************************/

		torrents = new JTable(new TorrentTableModel(TorrentManager.getInstance(), c));
		torrents.getColumnModel().getColumn(2).setCellRenderer(new ProgressCellRenderer());
		scrollPane.setViewportView(torrents);
		springLayout.putConstraint(SpringLayout.SOUTH, buttonPanel, 45, SpringLayout.NORTH, frmBitme.getContentPane());
		buttonPanel.setBackground(SystemColor.window);
		springLayout.putConstraint(SpringLayout.NORTH, buttonPanel, 0, SpringLayout.NORTH, frmBitme.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, buttonPanel, 0, SpringLayout.WEST, bottomPanel);
		springLayout.putConstraint(SpringLayout.EAST, buttonPanel, 0, SpringLayout.EAST, bottomPanel);
		torrents.setFillsViewportHeight(true);


        /**********************************************************************
         *                      TORRENT ACTIVE SETUP                          *
         **********************************************************************/

        nbActiveTransfers = new JTextField();
        nbActiveTransfers.setHorizontalAlignment(SwingConstants.CENTER);
        nbActiveTransfers.setText("0 transfert");
        nbActiveTransfers.setBackground(SystemColor.window);
        bottomPanel.add(nbActiveTransfers);
        nbActiveTransfers.setColumns(10);

        frmBitme.getContentPane().add(buttonPanel);
        SpringLayout sl_buttonPanel = new SpringLayout();
        buttonPanel.setLayout(sl_buttonPanel);

        /**********************************************************************
         *                           BUTTONS SETUP                            *
         **********************************************************************/

		JButton btnCreatetorrent = new JButton("");
		btnCreatetorrent.addActionListener(e -> MainWindow.this.createTorrentFile());
		btnCreatetorrent.setToolTipText("Créer un fichier Torrent");
		sl_buttonPanel.putConstraint(SpringLayout.NORTH, btnCreatetorrent, 5, SpringLayout.NORTH, buttonPanel);
		sl_buttonPanel.putConstraint(SpringLayout.WEST, btnCreatetorrent, 5, SpringLayout.WEST, buttonPanel);
		btnCreatetorrent.setIcon(new ImageIcon(MainWindow.class.getResource("/main/assets/createFilemin.png")));
		buttonPanel.add(btnCreatetorrent);

		JButton btnAddtorrent = new JButton("");
		btnAddtorrent.addActionListener(e -> MainWindow.this.openTorrentFile());
		btnAddtorrent.setToolTipText("Ajouter un fichier Torrent");
		sl_buttonPanel.putConstraint(SpringLayout.NORTH, btnAddtorrent, 5, SpringLayout.NORTH, buttonPanel);
		sl_buttonPanel.putConstraint(SpringLayout.WEST, btnAddtorrent, 5, SpringLayout.EAST, btnCreatetorrent);
		btnAddtorrent.setIcon(new ImageIcon(MainWindow.class.getResource("/main/assets/openFile.png")));
		buttonPanel.add(btnAddtorrent);

        JButton btnRemovetorrent = new JButton("");
        btnRemovetorrent.setEnabled(false);
        btnRemovetorrent.setToolTipText("Supprimer un fichier Torrent");
        btnRemovetorrent.addActionListener(e -> {
            ((TorrentTableModel)torrents.getModel()).removeTorrent(torrents.getSelectedRows());
            PeersTableModel peersModel = (PeersTableModel)MainWindow.this.peersTable.getModel();
            peersModel.fireTableDataChanged();
            updateActiveTorrents();
        });
        sl_buttonPanel.putConstraint(SpringLayout.SOUTH, btnRemovetorrent, 0, SpringLayout.SOUTH, btnCreatetorrent);
        btnRemovetorrent.setIcon(new ImageIcon(MainWindow.class.getResource("/main/assets/remove.png")));
        sl_buttonPanel.putConstraint(SpringLayout.NORTH, btnRemovetorrent, 5, SpringLayout.NORTH, buttonPanel);
        sl_buttonPanel.putConstraint(SpringLayout.WEST, btnRemovetorrent, 45, SpringLayout.EAST, btnAddtorrent);
        buttonPanel.add(btnRemovetorrent);

        JButton btnPauseTorrent = new JButton("");
        btnPauseTorrent.setToolTipText("Mettre en pause");
        btnPauseTorrent.addActionListener(e -> {
            ((TorrentTableModel)torrents.getModel()).pauseTorrent(torrents.getSelectedRows());
        });
        sl_buttonPanel.putConstraint(SpringLayout.SOUTH, btnPauseTorrent, 0, SpringLayout.SOUTH, btnCreatetorrent);
        btnPauseTorrent.setIcon(new ImageIcon(MainWindow.class.getResource("/main/assets/pause.png")));
        sl_buttonPanel.putConstraint(SpringLayout.NORTH, btnPauseTorrent, 5, SpringLayout.NORTH, buttonPanel);
        sl_buttonPanel.putConstraint(SpringLayout.WEST, btnPauseTorrent, 45, SpringLayout.EAST, btnRemovetorrent);
        buttonPanel.add(btnPauseTorrent);

        JButton btnStartTorrent = new JButton("");
        btnStartTorrent.setToolTipText("Demarrer le téléchargement");
        btnStartTorrent.addActionListener(e -> {
            ((TorrentTableModel)torrents.getModel()).startTorrent(torrents.getSelectedRows());
        });
        sl_buttonPanel.putConstraint(SpringLayout.SOUTH, btnStartTorrent, 0, SpringLayout.SOUTH, btnCreatetorrent);
        btnStartTorrent.setIcon(new ImageIcon(MainWindow.class.getResource("/main/assets/start.png")));
        sl_buttonPanel.putConstraint(SpringLayout.NORTH, btnStartTorrent, 5, SpringLayout.NORTH, buttonPanel);
        sl_buttonPanel.putConstraint(SpringLayout.WEST, btnStartTorrent, 5, SpringLayout.EAST, btnPauseTorrent);
        buttonPanel.add(btnStartTorrent);

        /**********************************************************************
         *                    POST-INITIALIZATION SETUP                       *
         **********************************************************************/

		torrents.getSelectionModel().addListSelectionListener(e -> {
            if (torrents.getSelectedRow() == -1) {
                btnRemovetorrent.setEnabled(false);
                mntmEffacer.setEnabled(false);
            } else {
                btnRemovetorrent.setEnabled(true);
                mntmEffacer.setEnabled(true);

                TorrentFile t = ((TorrentTableModel)torrents.getModel()).getTorrentAt(torrents.getSelectedRow());
                PeersTableModel peersModel = (PeersTableModel)peersTable.getModel();
                peersModel.setTorrent(t);
                peersModel.fireTableDataChanged();

                ((PiecesPanel)piecesPanel).setTorrentFile(t);
            }
        });

		mntmToutSlectionner.addActionListener(e -> torrents.setRowSelectionInterval(0, torrents.getModel().getRowCount() - 1));

        applyKeyListner(this.frmBitme);
	}

	private void finish() {
        splitPanel.setDividerLocation(splitPanel.getSize().height /2);
    }

	/*
	 * Method applying the KeyListener to all the components of the window
	 */
	private List<Component> applyKeyListner(final Container c) {
        Component[] comps = c.getComponents();
        List<Component> compList = new ArrayList<>();
        for (Component comp : comps) {
            compList.add(comp);
            comp.addKeyListener(new MyKeyListener());
            if (comp instanceof Container)
                compList.addAll(applyKeyListner((Container) comp));
        }
        return compList;
    }

    /*
     * Open a torrent file and add it to the TorrentManager
     * Prompt the user twice. The first time for the torrent file, the second
     * for the saving location.
     */
	private void openTorrentFile() {
		fc.setDialogTitle("Ouvrir un fichier Torrent");
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setFileFilter(new FileNameExtensionFilter("Fichier Torrent", "torrent"));
		int returnVal = fc.showOpenDialog(MainWindow.this.frmBitme);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			fc.setDialogTitle("Ou enregistrer le fichier");
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			returnVal = fc.showOpenDialog(MainWindow.this.frmBitme);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
                ((TorrentTableModel) torrents.getModel()).addTorrent(file.getAbsolutePath(), fc.getSelectedFile().getAbsolutePath());
                updateActiveTorrents();
            }
		} else {
			System.out.println("Open command cancelled by user.");
		}
	}

    /*
     * Create a torrent file and save it to the disk
     * Prompt the user twice. The first time for the file to turn into torrent,
     * the second for the saving location.
     */
	private void createTorrentFile() {
        fc.setDialogTitle("Créer un fichier Torrent");
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int returnVal = fc.showOpenDialog(MainWindow.this.frmBitme);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selected = fc.getSelectedFile();
            AddTorrentDialog.display(selected, TorrentManager.getInstance());
        }
	}

	/*
	 * Update the bottom number of active torrents in the bottom bar
	 */
	private void updateActiveTorrents() {
        nbActiveTransfers.setText(torrents.getModel().getRowCount() + " transfers");
    }

    /*
     * Action for the KeyListener
     */
    enum ACTION { OPEN, CREATE, NONE}

    /*
     * KeyListener, can open and create a torrent (for now)
     */
	class MyKeyListener extends KeyAdapter {

	    private List<Integer> events = new ArrayList<>();

		@Override
		public void keyPressed(KeyEvent e) {
			this.events.add(e.getKeyCode());
			boolean cmdPressed = false;
            MainWindow.ACTION action = ACTION.NONE;
			for (Integer event : events) {
                if (event == KeyEvent.VK_META || KeyEvent.CTRL_MASK != 0)
                    cmdPressed = true;
                if (event == KeyEvent.VK_O)
                    action = ACTION.OPEN;
                if (event == KeyEvent.VK_C)
                    action = ACTION.CREATE;
            }
            if (!cmdPressed)
                return;
			switch (action) {
                case OPEN:
                    MainWindow.this.openTorrentFile();
                    this.events.clear();
                    break;
                case CREATE:
                    MainWindow.this.createTorrentFile();
                    this.events.clear();
                    break;
                case NONE:
                    break;
            }

		}

		@Override
		public void keyReleased(KeyEvent e) {
		    if (this.events.indexOf(e.getKeyCode()) != -1)
                this.events.remove(this.events.indexOf(e.getKeyCode()));
		}
	}
}
