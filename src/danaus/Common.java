package danaus;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import javax.imageio.ImageIO;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A utility class with a miscellaneous set of functions used commonly by 
 * Danaus' code. 
 */
public class Common {
	/**
	 * Return a mod n. Note that -1 mod 5 = 4. For negative a, a mod 5 and a % 5
	 * are different.
	 * 
	 * @param a The 'a' in a mod n.
	 * @param n The 'n' in a mod n.
	 * @return a mod n. 
	 */
	public static int mod(int a, int n) {
		return ((a % n) + n ) % n;
	}
	
	/**
	 * @see danaus.Common#mod(int, int)
	 */
	public static float mod(float a, float n) {
		return ((a % n) + n ) % n;
	}
	
	/**
	 * Return the number of times c appears in s.
	 * 
	 * @param s A string to search.
	 * @param c A character to look for.
	 * @return The number of occurrences of c in s. 
	 */
	public static int numberOfOccurrences(String s, char c) {
		Debugger.NULL_CHECK(s, "null string in countOccurences");
		
		int count= 0;
		
		for (int i= 0; i < s.length(); i++) {
			if (s.charAt(i) == c) {
				count++;
			}
		}
		
		return count;
	}
	
	/**
     * Return the number of times sub appears in s. Note that overlapping
	 * occurrences are counted, e.g. numberOfOccurrences("eee", String "ee")
	 * is 2.
	 * 
	 * @param s A string to search.
	 * @param sub A substring to look for.
	 * @return The number of occurrences of sub in s. 
	 */
	public static int numberOfOccurrences(String s, String sub) {
		Debugger.NULL_CHECK(s, "null string in countOccurences!");
		Debugger.NULL_CHECK(sub, "null substring in countOccurences!");
		
		int count= 0;
		int lookIndex= 0;
		
		while (s.indexOf(sub, lookIndex) != -1) {
			lookIndex= s.indexOf(sub, lookIndex) + 1;
			count++;
		}
		
		return count;
	}
	
	/**
	 * Returns the text of the first child element with the given tagname or 
	 * null if no such child element exists.
	 * 
	 * @param element The parent element.
	 * @param tagname The tagname of the child element to get text from.
	 * @return The text of the child with the given tagname, or null if no such
	 * child exists.
	 * @throws NullPointerException if either parameter is null.
	 */
	public static String get_text_by_tag_name(Element element, String tagname) {
		// If element is null, likely something is wrong elsewhere
		if (element == null) {
			String tag;
			if (tagname == null) {
				tag = "with null tagname";
			}
			else {
				tag = "with tagname " + tagname;
			}
			
			Debugger.ERROR("null element in getTextByTagName " + tag);
			throw new NullPointerException();
		}
		
		// nodelist that should contain either no elements or one element, the
		// child with the given tagname
		NodeList elements = element.getElementsByTagName(tagname);
		
		if (elements.getLength() == 0) {
			return null;
		}
		
		return elements.item(0).getTextContent();
	}
	
	/**
	 * Return true iff s is not "no". 
	 * 
	 * @param s A string.
	 * @return True if s is not "no", false in all other cases.
	 */
	public static boolean isNotNo(String s) {
	    return s == null  ||  !s.toLowerCase().equals("no");
	}
	
	/**
	 * If s contains a non-negative integer, return it. <br>
	 * If s contains a negative integer, return 0. <br>
	 * If s is null, return _default. <br>
	 * If s is anything else, display message warning and return _default.
	 * 
	 * @param s The string from which to extract an int.
	 * @param _default The default value to return if an exception occurs.
	 * @param warning The warning message to display if parsing fails.
	 * @return An int extracted from s or a default value if something fails.
	 */
	public static int intValueOf(String s, int _default, String warning) {
		if (s == null) {
			return _default;
		}
		try {
			int i= Math.max(0, Integer.parseInt(s));
			return i;
		}
		catch (RuntimeException e) {
			//Debugger.WARNING(warning);
			return _default;
		}
	}
	
	/**
	 * @see danaus.Common#intValueOf(String, int, String)
	 */
	public static double doubleValueOf(String s, double _default, String warning) {
		if (null == s) {
			return _default;
		}
		try {
			double d = Math.max(0, Double.parseDouble(s));
			return d;
		}
		catch (RuntimeException e) {
			Debugger.WARNING(warning);
			return _default;
		}
	}
	
	/**
	 * @see danaus.Common#intValueOf(String, int, String)
	 */
	public static Wind windValueOf(String s, Wind _default, String warning) {
		if (null == s) {
			return _default;
		}
		try {
			Wind w = Wind.parseWind(s);
			return w;
		}
		catch (RuntimeException e) {
			//Debugger.WARNING(warning);
			return _default;
		}
	}

	/**
	 * Returns a unit value with the same size as the integer, or 0 if the int
	 * is 0.
	 * 
	 * @param i An int.
	 * @return -1 if i < 0
	 *          0 if i == 0
	 *          1 if i > 0
	 */
	public static int unit_scalar(int i) {
		if (i < 0) {
			return -1;
		}
		if (i > 0) {
			return 1;
		}
		return 0;
	}
	
	/**
	 * Returns a loaded image whether Danaus is run via Eclipse or via the
     * command line. All image filenames should be made relative to Danaus'
     * root directory. For example "res/tiles/foo.png" loads
     * "DANAUS_ROOT/res/tiles/foo.png".
	 * 
	 * @param filename The filename of an image relative the Danaus' root 
     *                 directory
	 * @return The loaded image. 
	 */
	public static BufferedImage load_image(String filename) {
		try {
			return ImageIO.read(new File(absolute_path() + "../" + filename));
		}
		catch (IOException e) {
			Debugger.ERROR("Could not load image " + filename);
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}

	/**
     * Acquire semaphore sem, or exit with error 1 if it is interrupted.
	 * 
	 * @param sem A semaphore to acquire.
	 */
	public static void acquire_or_exit(Semaphore sem) {
		try {
			sem.acquire();
		} catch (InterruptedException e) {
			Debugger.ERROR("Semaphore acquire interuptted.");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Returns the absolute path of the bin directory of Danaus' .class files.
	 * 
	 * @return Absolute path of Danaus' bin directory.
	 */
	public static String absolute_path() {
		Class<Simulator> c= Simulator.class;
        String s = c.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (System.getProperty("os.name").contains("Windows")) {
                return s.substring(1);
        }
        else {
                return s;
        }
	}
	
	/**
	 * Changes the brightness of an image. A scale of 1.0 does not change the
	 * brightness of an image. A scale of 0.5 halves the brightness. A scale of
	 * 2.0 doubles the brightness. <br>
	 * 
	 * Borrowed verbatim from
	 * http://stackoverflow.com/questions/12980780/how-to-change-the-brightness-of-an-image
	 * 
	 * @param img An image.
	 * @param scale A scale, as described above.
	 */
	public static void change_brightness(BufferedImage img, float scale) {
		Graphics g= img.getGraphics();
		int brightness= (int)(256 - 256 * scale);
		g.setColor(new Color(0,0,0,brightness));
		g.fillRect(0, 0, img.getWidth(), img.getHeight());
	}
}
