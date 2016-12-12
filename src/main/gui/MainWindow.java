package main.gui;


import main.Client;
import main.torrent.TorrentManager;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

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
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    try {
                        MainWindow window = new MainWindow(c);
                        window.frmBitme.setVisible(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
		frmBitme.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


		JMenuBar menuBar = new JMenuBar();
		frmBitme.setJMenuBar(menuBar);

		JMenu mnFichier = new JMenu("Fichier");
		menuBar.add(mnFichier);

		JMenuItem mntmCrerUnFichier = new JMenuItem("Créer un fichier torrent");
        mnFichier.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { MainWindow.this.createTorrentFile(); }
        });
		mnFichier.add(mntmCrerUnFichier);

		JMenuItem mntmOuvrirUnFichier = new JMenuItem("Ouvrir un fichier torrent");
		mntmOuvrirUnFichier.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainWindow.this.openTorrentFile();
			}
		});
		mnFichier.add(mntmOuvrirUnFichier);

		JMenuItem mntmOuvrirUnLien = new JMenuItem("Ouvrir un lien magnet");
		mntmOuvrirUnLien.setEnabled(false);
		mnFichier.add(mntmOuvrirUnLien);

		JSeparator separator = new JSeparator();
		mnFichier.add(separator);

		JMenuItem mntmFermerLaFentre = new JMenuItem("Fermer la fenêtre");
		mntmFermerLaFentre.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { System.exit(0); }
        });
		mnFichier.add(mntmFermerLaFentre);

		JMenu mndition = new JMenu("Édition");
		menuBar.add(mndition);

		JMenuItem mntmEffacer = new JMenuItem("Effacer");
		mntmEffacer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((TorrentTableModel)torrents.getModel()).removeTorrent(torrents.getSelectedRows());
                updateActiveTorrents();
            }
        });
		mndition.add(mntmEffacer);

		JMenuItem mntmToutSlectionner = new JMenuItem("Tout sélectionner");
		mntmToutSlectionner.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                torrents.setRowSelectionInterval(0, torrents.getModel().getRowCount());
            }
        });
		mndition.add(mntmToutSlectionner);

		JMenuItem mntmToutDslectionner = new JMenuItem("Tout désélectionner");
		mntmToutDslectionner.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                torrents.setRowSelectionInterval(0, 0);
                torrents.getSelectionModel().removeIndexInterval(0, torrents.getModel().getRowCount());
            }
        });
		mndition.add(mntmToutDslectionner);

		JMenu mnTransferts = new JMenu("Transferts");
		menuBar.add(mnTransferts);

		JMenu mnFentre = new JMenu("Fenêtre");
		menuBar.add(mnFentre);
		SpringLayout springLayout = new SpringLayout();
		frmBitme.getContentPane().setLayout(springLayout);

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

		torrents = new JTable(new TorrentTableModel(TorrentManager.getInstance(), c));
		torrents.getColumnModel().getColumn(2).setCellRenderer(new ProgressCellRenderer());
		scrollPane.setViewportView(torrents);
		springLayout.putConstraint(SpringLayout.SOUTH, buttonPanel, 45, SpringLayout.NORTH, frmBitme.getContentPane());
		buttonPanel.setBackground(SystemColor.window);
		springLayout.putConstraint(SpringLayout.NORTH, buttonPanel, 0, SpringLayout.NORTH, frmBitme.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, buttonPanel, 0, SpringLayout.WEST, bottomPanel);
		springLayout.putConstraint(SpringLayout.EAST, buttonPanel, 0, SpringLayout.EAST, bottomPanel);
		//torrents.addKeyListener(new MyKeyListener());
		torrents.setFillsViewportHeight(true);


		nbActiveTransfers = new JTextField();
		nbActiveTransfers.setHorizontalAlignment(SwingConstants.CENTER);
		nbActiveTransfers.setText("0 transfert");
		nbActiveTransfers.setBackground(SystemColor.window);
		bottomPanel.add(nbActiveTransfers);
		nbActiveTransfers.setColumns(10);
		frmBitme.getContentPane().add(buttonPanel);
		SpringLayout sl_buttonPanel = new SpringLayout();
		buttonPanel.setLayout(sl_buttonPanel);

		JButton btnCreatetorrent = new JButton("");
		btnCreatetorrent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainWindow.this.createTorrentFile();
			}
		});
		btnCreatetorrent.setToolTipText("Créer un fichier Torrent");
		sl_buttonPanel.putConstraint(SpringLayout.NORTH, btnCreatetorrent, 5, SpringLayout.NORTH, buttonPanel);
		sl_buttonPanel.putConstraint(SpringLayout.WEST, btnCreatetorrent, 5, SpringLayout.WEST, buttonPanel);
		btnCreatetorrent.setIcon(new ImageIcon(MainWindow.class.getResource("/main/assets/createFilemin.png")));
		buttonPanel.add(btnCreatetorrent);

		JButton btnAddtorrent = new JButton("");
		btnAddtorrent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainWindow.this.openTorrentFile();
			}
		});
		btnAddtorrent.setToolTipText("Ajouter un fichier Torrent");
		sl_buttonPanel.putConstraint(SpringLayout.NORTH, btnAddtorrent, 5, SpringLayout.NORTH, buttonPanel);
		sl_buttonPanel.putConstraint(SpringLayout.WEST, btnAddtorrent, 5, SpringLayout.EAST, btnCreatetorrent);
		btnAddtorrent.setIcon(new ImageIcon(MainWindow.class.getResource("/main/assets/openFilemin.png")));
		buttonPanel.add(btnAddtorrent);

		JButton btnRemovetorrent = new JButton("");
		btnRemovetorrent.setEnabled(false);
		btnRemovetorrent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((TorrentTableModel)torrents.getModel()).removeTorrent(torrents.getSelectedRows());
                updateActiveTorrents();
			}
		});

		torrents.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (torrents.getSelectedRow() == -1) {
                    btnRemovetorrent.setEnabled(false);
                    mntmEffacer.setEnabled(false);
                }
				else {
                    btnRemovetorrent.setEnabled(true);
                    mntmEffacer.setEnabled(true);
                }
			}
		});

		mntmToutSlectionner.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				torrents.setRowSelectionInterval(0, torrents.getModel().getRowCount() - 1);
			}
		});

		sl_buttonPanel.putConstraint(SpringLayout.SOUTH, btnRemovetorrent, 0, SpringLayout.SOUTH, btnCreatetorrent);
		btnRemovetorrent.setIcon(new ImageIcon(MainWindow.class.getResource("/main/assets/removeFile.png")));
		sl_buttonPanel.putConstraint(SpringLayout.NORTH, btnRemovetorrent, 5, SpringLayout.NORTH, buttonPanel);
		sl_buttonPanel.putConstraint(SpringLayout.WEST, btnRemovetorrent, 45, SpringLayout.EAST, btnAddtorrent);
		buttonPanel.add(btnRemovetorrent);
	}

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

	private void createTorrentFile() {
		fc.setDialogTitle("Créer un fichier Torrent");
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
                // TODO Do the actual saving
            }
		} else {
			System.out.println("Open command cancelled by user.");
		}
	}

	private void updateActiveTorrents() {
        nbActiveTransfers.setText(torrents.getModel().getRowCount() + " transfers");
    }

	/* static class MyKeyListener extends KeyAdapter {
		@Override
		public void keyTyped(KeyEvent e) {
			if (e.getKeyChar() == KeyEvent.VK_CAPS_LOCK ) {
				// delete row method (when "delete" is typed)
				System.out.println("Key \"Delete\" Typed");
			}
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_CAPS_LOCK ) {
				// delete row method (when "delete" is pressed)
				System.out.println("Key \"Delete\" Pressed");
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_CAPS_LOCK ) {
				// delete row method (when "delete" is released)
				System.out.println("Key \"Delete\" Released");
			}
		}
	}*/
}
