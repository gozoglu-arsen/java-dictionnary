package newpackage;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import javax.swing.*;
import java.net.*;
import java.io.*;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author GOZOGLU Arsen, ABDELQADER Fadi, KAMALI Yassine
 */
public class NewJFrame extends javax.swing.JFrame {

	/**
	 * Creates new form NewJFrame
	 */
	private Properties prop;
	private FileInputStream in;

	public static void affiche(String str) {
		//Affiche un message.
		JOptionPane.showMessageDialog(null, str);
	}

	private void enregistrer_un_mot(String mot_a_enregistrer, String definition) {
		//Cette fonction permet d'enregistrer un mot en utilisant la classe Property.
		// On met le mot a enregistrer en minuscule de sorte à ne pas avoir plusieurs définitions pour un même mot.
		try {
			FileOutputStream out = new FileOutputStream("Fichiers/Mots");
			prop.setProperty(mot_a_enregistrer.toLowerCase(), definition);
			prop.store(out, null);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getUrlContents(String theUrl) {
		//Recupère le code source d'un lien.
		//Fonction récupérée depuis internet.

		StringBuilder content = new StringBuilder();

		// many of these calls can throw exceptions, so i've just
		// wrapped them all in one try/catch statement.
		try {
			// create a url object
			URL url = new URL(theUrl);

			// create a urlconnection object
			URLConnection urlConnection = url.openConnection();
                        urlConnection.addRequestProperty("User-Agent", 
"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
			// wrap the urlconnection in a bufferedreader
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

			String line;

			// read from the urlconnection via the bufferedreader
			while ((line = bufferedReader.readLine()) != null) {
				content.append(line + "\n");
			}
			bufferedReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return content.toString();
	}

	private static String extraireLienImage(String mot) {

		// Permet d'obtenir le lien d'une illustration d'un mot depuis le site GettyImage
		//GettyImage ne masque pas son code source avec javascript, ce qui facilite la tâche.
		//On remplace les accents parce-que le site GettyImage ne les reconnais pas.
		mot = mot.replace(' ', '-');
		mot = mot.replace('é', 'e');
		mot = mot.replace('è', 'e');
		mot = mot.replace('â', 'a');
		mot = mot.replace('ê', 'e');
		mot = mot.replace('î', 'i');
		mot = mot.replace('ç', 'c');
		mot = mot.replace('à', 'a');
                
		String mydata = getUrlContents("https://www.gettyimages.fr/photos/" + mot + "?family=creative&license=rf&phrase=" + mot + "&sort=mostpopular");
		Pattern pattern = Pattern.compile("media\\.gettyimages\\.com\\/photos\\/(.*?)k=6");
		Matcher matcher = pattern.matcher(mydata);
		if (matcher.find()) {
			return "http://media.gettyimages.com/photos/" + matcher.group(1);
		}
		return "";

	}

	private static String extraireDefinition(String mot) {
		//Permet d'obtenir la définition d'un mot depuis le site larousse.

		String mydata = getUrlContents("https://www.larousse.fr/dictionnaires/francais/" + mot);
		Pattern pattern = Pattern.compile("<ul class=\\\"Definitions\\\">(((?s).)*?)<\\/ul>");
		Matcher matcher = pattern.matcher(mydata);
		if (matcher.find()) {
			return matcher.group(1);
		}

		return "";
	}

	public static BufferedImage toBufferedImage(Image img) {

		//Permet de convertir une image en BufferedImage, ce sera utile pour sauvegarder une image
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}

		// Create a buffered image with transparency
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();

		// Return the buffered image
		return bimage;
	}

	public NewJFrame() {
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		initComponents();
		setTitle("Dictionnaire");
		def_mot.setContentType("text/html");

		prop = new Properties();
		try {
			in = new FileInputStream("Fichiers/Mots");
			prop.load(in);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		prop.keySet().forEach(x -> jComboBox1.addItem(x.toString())); // Chargement des mots dans la jComboBox
		setLocation((int) dimension.getWidth() / 2 - getWidth() / 2, (int) dimension.getHeight() / 2 - getHeight() / 2); //On fait en sorte que la fenetre s'affiche au centre

		try {

			//Définition des icones pour les boutons et de la police
			Image img = ImageIO.read(new File("Fichiers/remove.png"));
			Remove.setIcon(new ImageIcon(img));
			Remove.setFont(new Font("Serif", Font.BOLD, 11));

			img = ImageIO.read(new File("Fichiers/add.png"));
			ModifDef.setIcon(new ImageIcon(img));
			ModifDef.setFont(new Font("Serif", Font.BOLD, 11));

			img = ImageIO.read(new File("Fichiers/image.png"));
			ModifImage.setIcon(new ImageIcon(img));
			ModifImage.setFont(new Font("Serif", Font.BOLD, 11));

			img = ImageIO.read(new File("Fichiers/internet.png"));
			BouttonOk.setIcon(new ImageIcon(img));
			BouttonOk.setFont(new Font("Serif", Font.BOLD, 11));

			img = ImageIO.read(new File("Fichiers/hasard.png"));
			hasard.setIcon(new ImageIcon(img));
			hasard.setFont(new Font("Serif", Font.BOLD, 11));

		} catch (Exception ex) {
			System.out.println(ex);
		}

		//Si il n'y a rien dans la combobox, inutle que le bouton sois cliquable.
		if (jComboBox1.getItemCount() == 0) {
			ModifImage.setEnabled(false);
			setSize(505, getHeight()); //Sinon on la retrecie
			ModifImage.setText("Ajouter");
		}


	}

	boolean est_illustre(String mot_a_chercher) {
		//Cette fonction renvoie true si le mot a chercher est illustré(contient une image) false sinon.
		String[] nom_images;
		File f = new File("images");
		nom_images = f.list();
		for (String nom_fichier : nom_images) {
			if (nom_fichier.equals(mot_a_chercher)) {
				return true;
			}
		}
		return false;

	}

	private String obtenir_ligne_au_hasard(String nom_fichier) {
		//Permet d'obtenir une ligne au hasard d'un fichier texte

		String ligne = "";
		File file = new File("Fichiers/dico.txt");
		try {
			RandomAccessFile f = new RandomAccessFile(file, "r");
			long randomLocation = (long) (Math.random() * f.length());
			f.seek(randomLocation);
			f.readLine();
			ligne = f.readLine();
			f.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ligne;
	}

	/**
	 * This method is called from within the constructor to initialize the
	 * form. WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        ModifDef = new javax.swing.JButton();
        Remove = new javax.swing.JButton();
        mot = new javax.swing.JTextField();
        jComboBox1 = new javax.swing.JComboBox<>();
        BouttonOk = new javax.swing.JButton();
        label1 = new java.awt.Label();
        ModifImage = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane2 = new javax.swing.JScrollPane();
        def_mot = new javax.swing.JTextPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        hasard = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(0, 51, 51));
        setPreferredSize(new java.awt.Dimension(837, 620));
        setResizable(false);

        jPanel3.setBackground(new java.awt.Color(255, 255, 204));

        ModifDef.setBackground(new java.awt.Color(153, 255, 153));
        ModifDef.setText("Ajouter");
        ModifDef.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ModifDefActionPerformed(evt);
            }
        });

        Remove.setBackground(new java.awt.Color(255, 0, 51));
        Remove.setText("Supprimer");
        Remove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RemoveActionPerformed(evt);
            }
        });

        mot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                motActionPerformed(evt);
            }
        });
        mot.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                motKeyPressed(evt);
            }
        });

        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        BouttonOk.setBackground(new java.awt.Color(0, 204, 204));
        BouttonOk.setText("Chercher");
        BouttonOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BouttonOkActionPerformed(evt);
            }
        });

        label1.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        label1.setText("Entrez un mot");

        ModifImage.setBackground(new java.awt.Color(255, 204, 51));
        ModifImage.setText("Ajouter/Modifier l'image");
        ModifImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ModifImageActionPerformed(evt);
            }
        });

        def_mot.setCaretColor(new java.awt.Color(0, 51, 51));
        def_mot.setMaximumSize(new java.awt.Dimension(62, 21));
        def_mot.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                def_motFocusLost(evt);
            }
        });
        def_mot.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                def_motMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(def_mot);

        jPanel2.setBackground(new java.awt.Color(255, 235, 135));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(18, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        hasard.setBackground(new java.awt.Color(204, 204, 255));
        hasard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hasardActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(ModifDef, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(61, 61, 61)
                        .addComponent(ModifImage, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(115, 115, 115)
                        .addComponent(Remove, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 394, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 386, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(93, 93, 93)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(mot, javax.swing.GroupLayout.PREFERRED_SIZE, 282, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(hasard, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(BouttonOk, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(305, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(hasard)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(mot, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(BouttonOk)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ModifDef)
                    .addComponent(ModifImage))
                .addGap(18, 18, 18)
                .addComponent(Remove)
                .addGap(73, 73, 73))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BouttonOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BouttonOkActionPerformed

	    if (!mot.getText().toString().isBlank()) {
		    Image image = null;
		    def_mot.setContentType("text/html"); //Les balises HTML sont interprétées.
		    def_mot.setText(extraireDefinition(mot.getText()));
		    try {
			    URL url = new URL(extraireLienImage(mot.getText()));
			    image = ImageIO.read(url);
			    Image newimg = image.getScaledInstance(320, 230, java.awt.Image.SCALE_SMOOTH); //On redimentionne l'image
			    ImageIcon icon = new ImageIcon(newimg);
			    jLabel1.setIcon(icon);
			    ImageIO.write(toBufferedImage(newimg), "png", new File("images/" + mot.getText().toString()));

		    } catch (IOException e) {
			    e.printStackTrace();

		    }

		    enregistrer_un_mot(mot.getText(), def_mot.getText());
		    if (obtenir_index(jComboBox1, mot.getText().toLowerCase()) == -1) { //Si le mot n'est pas déjà présent dans la jComBox, alors on l'ajoute
			    jComboBox1.addItem(mot.getText().toString().toLowerCase());
		    }

		    jComboBox1.setSelectedIndex(obtenir_index(jComboBox1, mot.getText().toString().toLowerCase())); //On change d'item automatiquement
	    } else {
		    affiche("Entrez un mot");
	    }


    }//GEN-LAST:event_BouttonOkActionPerformed

        private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
		// TODO add your handling code here:
		def_mot.setContentType("text/html");
		ModifImage.setEnabled(true);
		Image image = null;
		def_mot.setText(prop.getProperty(jComboBox1.getSelectedItem().toString())); //on Recupère la définition du mot(sa propriété)
		mot.setText(jComboBox1.getSelectedItem().toString());

		try {
			image = ImageIO.read(new File("images/" + jComboBox1.getSelectedItem().toString()));
			Image newimg = image.getScaledInstance(320, 230, java.awt.Image.SCALE_SMOOTH);
			ImageIcon icon = new ImageIcon(newimg);
			jLabel1.setVisible(true);
			jLabel1.setIcon(icon);

		} catch (IOException e) {
        }//GEN-LAST:event_jComboBox1ActionPerformed

		if (est_illustre(jComboBox1.getSelectedItem().toString())) { //Si le mot est illlustré, on change la largeur de la fenetre
			setSize(850, getHeight());
			ModifImage.setText("Modifier");
		} else {
			setSize(505, getHeight()); //Sinon on la retrecie
			ModifImage.setText("Ajouter");
		}

		ModifDef.setText("Modifier");
	}

	private int obtenir_index(JComboBox j, String mot_a_chercher) {

		//Cette fonction permet d'obtenir l'index d'un mot dans une jcombobox. Renvoie -1 si pas trouvé
		for (int i = 0; i < jComboBox1.getItemCount(); i++) {
			if (jComboBox1.getItemAt(i).toString().equals(mot_a_chercher)) {
				return i;
			}
		}
		return -1;
	}
        private void ModifDefActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ModifDefActionPerformed
		// TODO add your handling code here:
                
		if (!mot.getText().toString().isBlank() && !def_mot.getText().toString().isBlank()) { //Si les informations donnés sont correctes

			enregistrer_un_mot(mot.getText(), def_mot.getText());

			if (obtenir_index(jComboBox1, mot.getText().toString().toLowerCase()) == -1) {
				jComboBox1.addItem(mot.getText().toString().toLowerCase());
			}
			jComboBox1.setSelectedIndex(obtenir_index(jComboBox1, mot.getText().toString().toLowerCase()));

		} else
			affiche("Le mot et la définition ne doivent pas êtres vide.");
        }//GEN-LAST:event_ModifDefActionPerformed

        private void RemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RemoveActionPerformed
		// TODO add your handling code here:

		try {
			FileOutputStream out = new FileOutputStream("Fichiers/Mots");
			prop.remove(mot.getText()); //On supprime des propriétés le mot acuel
			prop.store(out, null);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (jComboBox1.getItemCount() >= 1) { //Si il y a au moin un item
			for (int i = 0; i < jComboBox1.getItemCount(); i++) {
				if (jComboBox1.getItemAt(i).toString().equals(mot.getText().toString())) {
					jComboBox1.removeItemAt(i);
				}
			}
			mot.setText(jComboBox1.getSelectedItem().toString());
			def_mot.setText(prop.getProperty(jComboBox1.getSelectedItem().toString()));
		} else { //Sinon on réajuste les paramètres
			def_mot.setContentType("text");
			mot.setText("");
			def_mot.setText("");
			jComboBox1.removeAllItems();
			jLabel1.setVisible(false);
			setSize(505, getHeight());
			ModifImage.setEnabled(false);
			ModifDef.setText("Ajouter");
			ModifImage.setText("Ajouter");

		}
        }//GEN-LAST:event_RemoveActionPerformed

        private void ModifImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ModifImageActionPerformed
		// TODO add your handling code here:
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
			"Image", "jpg", "png", "bmp", "jpeg");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			try {
				Image image;
				image = ImageIO.read(new File(chooser.getSelectedFile().getAbsolutePath()));
				Image newimg = image.getScaledInstance(320, 230, java.awt.Image.SCALE_SMOOTH);
				ImageIcon icon = new ImageIcon(newimg);
				jLabel1.setVisible(true);
				jLabel1.setIcon(icon);
				ImageIO.write(toBufferedImage(newimg), "png", new File("images/" + jComboBox1.getSelectedItem().toString()));

			} catch (IOException e) {
				e.printStackTrace();
			}

			setSize(850, getHeight());
		}
        }//GEN-LAST:event_ModifImageActionPerformed

        private void motActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_motActionPerformed
		// TODO add your handling code here:

        }//GEN-LAST:event_motActionPerformed

        private void motKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_motKeyPressed
		// TODO add your handling code here:
		ModifDef.setText("Ajouter");
        }//GEN-LAST:event_motKeyPressed

        private void hasardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hasardActionPerformed
		// TODO add your handling code here:
		mot.setText(obtenir_ligne_au_hasard("dico.txt"));
        }//GEN-LAST:event_hasardActionPerformed

        private void def_motMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_def_motMouseClicked
		// TODO add your handling code here:


        }//GEN-LAST:event_def_motMouseClicked

        private void def_motFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_def_motFocusLost

        }//GEN-LAST:event_def_motFocusLost

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
		//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
		/* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
		 */
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		//</editor-fold>

		//SPLASHSCREEN
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) ((dimension.getWidth() / 2 - 400));
		int y = (int) ((dimension.getHeight() / 2 - 250));
		JWindow window = new JWindow();
		window.getContentPane().add(
			new JLabel("", new ImageIcon("Fichiers/splash.png"), SwingConstants.CENTER));
		window.setBounds(x, y, 800, 500);
		window.setVisible(true);
		try {
			Thread.sleep(1200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		window.setVisible(false);
		window.dispose();

		//FIN SPLASHSCREEN
		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new NewJFrame().setVisible(true);

			}
		});
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BouttonOk;
    private javax.swing.JButton ModifDef;
    private javax.swing.JButton ModifImage;
    private javax.swing.JButton Remove;
    private javax.swing.JTextPane def_mot;
    private javax.swing.JButton hasard;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private java.awt.Label label1;
    private javax.swing.JTextField mot;
    // End of variables declaration//GEN-END:variables
}
