package pl.mpak.sky.gui.swing;

import java.awt.Toolkit;
import java.io.File;
import java.net.URL;
import java.util.HashMap;

import javax.swing.ImageIcon;

/**
 * Klasa �aduj�ca (o ile to konieczne) obrazki z zasob�w programu
 * 
 * @author Andrzej Ka�u�a
 *
 */
public final class ImageManager {
  
  private static HashMap<String, ImageIcon> imageList = new HashMap<String, ImageIcon>();
  public static String iconPath = "/pl/mpak/res/icons";
  
  /**
   * Zwraca obrazek z listy lub je�li jeszcze nie jest za�adowany �aduje go
   * 
   * @param resName nazwa obrazka do pobrania
   * @return pobrany obrazek
   */
  public final static ImageIcon getImage(String resName) {
    return getImage(resName, (Class<?>) null);
  }

  private final static ImageIcon internalGetImage(String resName, Class<?> rootClass) {
    try {
      URL url;
      File file = new File(resName);
      if (file.exists()) {
        url = file.toURI().toURL();
      }
      else if (rootClass != null) {
        url = rootClass.getResource(resName);
      }
      else {
        // Use context classloader so custom classloaders (e.g. StartupClassLoader)
        // are consulted — Class.getResource() on a platform-loaded class (WToolkit)
        // cannot see application resources on Java 9+.
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String stripped = resName.startsWith("/") ? resName.substring(1) : resName;
        url = cl.getResource(stripped);
      }
      return new ImageIcon(url);
    }
    catch (Throwable e) {
      return null;
    }
  }
  
  public final static ImageIcon getImage(String resName, Class<?> rootClass) {
    synchronized (imageList) {
      ImageIcon ii = imageList.get(resName);
      if (ii != null) {
        return ii;
      }
      ii = internalGetImage(resName, rootClass);
      if (ii == null) {
        ii = internalGetImage(iconPath +"/" +resName +".png", rootClass);
        if (ii == null) {
          ii = internalGetImage(iconPath +"/" +resName +".gif", rootClass);
        }
      }
      if (ii != null) {
        imageList.put(resName, ii);
        return ii;
      }
      imageList.put(resName, null);
      return null;
    }
  }
  
}
