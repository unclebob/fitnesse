package util;


// https://stackoverflow.com/questions/21682761/reading-a-log-file-and-displaying-it-in-jtextarea

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LogView {

  private JDialog dialog;
  private JTextArea textArea;
  private final String title;
  private final ImageIcon imageIcon;
  private boolean isClosing;
  private boolean isClosed;

  public LogView(String title, ImageIcon imageIcon) {
    this.title = title;
    this.imageIcon = imageIcon;
    SwingUtilities.invokeLater(new GUIRunnable());
  }
  public void append(final String text) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if(isClosed || isClosing) {
          return;
        }
        textArea.append(text);
      }
    });
  }
  public void dispose() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if(isClosed || isClosing) {
          return;
        }
        dialog.dispose();
      }
    });
  }

  private class GUIRunnable implements Runnable {
    public void run() {
      isClosing = false;
      isClosed = false;
      textArea = new JTextArea(25, 60);
      dialog = new JDialog((Window)null, title);
      if(imageIcon != null) {
        dialog.setIconImage(imageIcon.getImage());
      }
      dialog.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent windowEvent) {
          LogView.this.isClosing = true;
        }
        public void windowClosed(WindowEvent windowEvent) {
          LogView.this.isClosed = true;
        }
      });
      JScrollPane scrollPane = new JScrollPane(textArea);
      new SmartScroller(scrollPane, SmartScroller.VERTICAL, SmartScroller.END);
      dialog.add(scrollPane, BorderLayout.CENTER);
      dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      dialog.pack();
      dialog.setLocationByPlatform(true);
      dialog.setVisible(true);
    }
  }
  public static ImageIcon getImageIconFromPath(String imgPath) {
    ImageIcon imageIcon = null;
    java.net.URL imgURL = LogView.class.getClassLoader().getResource(imgPath);
    if (imgURL != null) {
      imageIcon = new ImageIcon(imgURL);
    } else {
      System.err.println("Couldn't find icon resource: " + imgPath);
    }
    return imageIcon;
  }
}
