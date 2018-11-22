package by.andd3dfx;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.RootDoc;

/**
 * Doclet used for just printing class names to console
 */
public class ListClassesDoclet {

  public static boolean start(RootDoc root) {
    for (ClassDoc classDoc : root.classes()) {
      System.out.println(classDoc);
    }
    return true;
  }
}
