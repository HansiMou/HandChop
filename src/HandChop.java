import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

/**
 * @author Hansi Mou
 * @date Mar 19, 2016
 * @version 1.0
 */

/**
 * @author Hansi Mou
 *
 *         Mar 19, 2016
 */
public class HandChop {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 IntializeSites();
		// JTidyHTMLHandler j = new JTidyHTMLHandler();
		// try {
		// j.getDocument(new File("a.txt"));
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	public static void IntializeSites() {
		try {
			// read file content from file
			FileReader reader = new FileReader("config.txt");
			BufferedReader br = new BufferedReader(reader);

			String str = null;

			while ((str = br.readLine()) != null) {
				String[] s = str.split(",");
				NameLink.put(s[0].trim().toLowerCase(), s[1].trim()
						.toLowerCase());
			}

			br.close();
			reader.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		}
		String[] test = new String[4];
		test[0] = NameLink.get("sephora") + "eye";
		test[1] = 1 + "";
		test[2] = "eye";
		test[3] = "1";
		WebCrawler wc = new WebCrawler();
		wc.run(test);
	}

	static HashMap<String, String> NameLink = new HashMap<String, String>();
}
