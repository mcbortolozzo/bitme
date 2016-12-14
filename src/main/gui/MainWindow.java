package main.gui;


import main.Client;
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

public class MainWindow {

	private JFrame frmBitme;
	private JTextField nbActiveTransfers;
	private JTable torrents;
	private final JFileChooser fc = new JFileChooser();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
        try {
            final Client c = new Client(Client.PORT);
			c.run();
            EventQueue.invokeLater(() -> {
                try {
                    MainWindow window = new MainWindow(c);
                    window.frmBitme.setVisible(true);
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

		JPanel mainPanel = new JPanel();
		mainPanel.setBackground(SystemColor.text);
		springLayout.putConstraint(SpringLayout.WEST, mainPanel, 0, SpringLayout.WEST, bottomPanel);
		springLayout.putConstraint(SpringLayout.SOUTH, mainPanel, 0, SpringLayout.NORTH, bottomPanel);
		springLayout.putConstraint(SpringLayout.EAST, mainPanel, 0, SpringLayout.EAST, bottomPanel);
		frmBitme.getContentPane().add(mainPanel);

		JPanel buttonPanel = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, mainPanel, 45, SpringLayout.NORTH, buttonPanel);
		SpringLayout sl_mainPanel = new SpringLayout();
		mainPanel.setLayout(sl_mainPanel);

        JScrollPane scrollPane = new JScrollPane();
        sl_mainPanel.putConstraint(SpringLayout.NORTH, scrollPane, 0, SpringLayout.NORTH, mainPanel);
        sl_mainPanel.putConstraint(SpringLayout.WEST, scrollPane, 0, SpringLayout.WEST, mainPanel);
        sl_mainPanel.putConstraint(SpringLayout.SOUTH, scrollPane, 0, SpringLayout.SOUTH, mainPanel);
        sl_mainPanel.putConstraint(SpringLayout.EAST, scrollPane, 0, SpringLayout.EAST, mainPanel);
        mainPanel.add(scrollPane);

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
		btnAddtorrent.setIcon(new ImageIcon(MainWindow.class.getResource("/main/assets/openFilemin.png")));
		buttonPanel.add(btnAddtorrent);

		JButton btnRemovetorrent = new JButton("");
		btnRemovetorrent.setEnabled(false);
		btnRemovetorrent.addActionListener(e -> {
            ((TorrentTableModel)torrents.getModel()).removeTorrent(torrents.getSelectedRows());
            updateActiveTorrents();
        });
        sl_buttonPanel.putConstraint(SpringLayout.SOUTH, btnRemovetorrent, 0, SpringLayout.SOUTH, btnCreatetorrent);
        btnRemovetorrent.setIcon(new ImageIcon(MainWindow.class.getResource("/main/assets/removeFile.png")));
        sl_buttonPanel.putConstraint(SpringLayout.NORTH, btnRemovetorrent, 5, SpringLayout.NORTH, buttonPanel);
        sl_buttonPanel.putConstraint(SpringLayout.WEST, btnRemovetorrent, 45, SpringLayout.EAST, btnAddtorrent);
        buttonPanel.add(btnRemovetorrent);

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
            }
        });

		mntmToutSlectionner.addActionListener(e -> torrents.setRowSelectionInterval(0, torrents.getModel().getRowCount() - 1));

        applyKeyListner(this.frmBitme);
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
                System.out.println("Opening: " + file.getAbsolutePath());
                System.out.println("Saving into: " + fc.getSelectedFile().getAbsolutePath());
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
		/*fc.setDialogTitle("Créer un fichier Torrent");
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int returnVal = fc.showOpenDialog(MainWindow.this.frmBitme);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            System.out.println("Opening: " + file.getAbsolutePath());
            String originName = file.getName();
            if (file.isFile())
                originName = originName.substring(0, originName.lastIndexOf('.'));
            String outName = originName + ".torrent";
            fc.setDialogTitle("Enregistrer le fichier torrent");
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fc.setFileFilter(new FileNameExtensionFilter("Fichier Torrent", "torrent"));
            fc.setCurrentDirectory(new File(file.getParentFile().getPath() + "/" + outName));
            System.out.println(file.getParentFile().getPath() + "/" + outName);
            returnVal = fc.showSaveDialog(MainWindow.this.frmBitme);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                System.out.println("Save file to " + fc.getSelectedFile().getAbsolutePath() + ".torrent");
            }
		} else {
			System.out.println("Open command cancelled by user.");
		}*/
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
