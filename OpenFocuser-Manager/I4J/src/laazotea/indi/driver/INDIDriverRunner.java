/*
 *  This file is part of INDI for Java Driver.
 * 
 *  INDI for Java Driver is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation, either version 3 of 
 *  the License, or (at your option) any later version.
 * 
 *  INDI for Java Driver is distributed in the hope that it will be
 *  useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 *  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with INDI for Java Driver.  If not, see 
 *  <http://www.gnu.org/licenses/>.
 */
package laazotea.indi.driver;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * A class to initialize and launch a
 * <code>INDIDriver</code>. It just contain a
 * <code>main</code> method to initialize the appropriate Driver.
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.10, March 19, 2012
 */
public class INDIDriverRunner {

  /**
   * Initializes a
   * <code>INDIDriver</code>.
   *
   * @param args the command line arguments. The first argument must be the
   * complete name of the class of the <code>INDIDriver</code>. That class must
   * be in the class path in order to be loaded.
   * @see INDIDriver
   */
  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println("A INDIDriver class name must be supplied");
      System.exit(-1);
    }

    INDIDriver driver = null;

    try {
      Class theClass = Class.forName(args[0]);
      Constructor c = theClass.getConstructor(InputStream.class, OutputStream.class);
      driver = (INDIDriver)c.newInstance(System.in, System.out);
    } catch (ClassNotFoundException ex) {
      System.err.println(ex + " class must be in class path.");
      System.exit(-1);
    } catch (InstantiationException ex) {
      System.err.println(ex + " class must be concrete.");
      System.exit(-1);
    } catch (IllegalAccessException ex) {
      System.err.println(ex + " class must have a no-arg constructor.");
      System.exit(-1);
    } catch (NoSuchMethodException ex) {
      System.err.println(ex + " class must have a InputStream, OutputStream constructor.");
      System.exit(-1);
    } catch (InvocationTargetException ex) {
      System.err.println(ex + " invocation target exception.");
      System.exit(-1);
    }

//    System.err.println(driver.getName());

    driver.startListening();
  }
}
