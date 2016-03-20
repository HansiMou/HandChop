import java.net.URL;

/**
 * @author Hansi Mou
 * @date Mar 14, 2016
 * @version 1.0
 */

/**
 * @author Hansi Mou
 *
 *         Mar 14, 2016
 */
public class ScoredLink {
	URL url;
	int score;
	String title = "";
	public ScoredLink(URL u, int s, String t) {
		this.url = u;
		this.score = s;
		this.title = t;
	}
}
