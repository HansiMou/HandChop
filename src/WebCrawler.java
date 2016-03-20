// A minimal Web Crawler written in Java
// Usage: From command line 
//     java WebCrawler <URL> [N]
//  where URL is the url to start the crawl, and N (optional)
//  is the maximum number of pages to download.

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.StringTokenizer;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class WebCrawler {
	public static final int SEARCH_LIMIT = 200; // Absolute max pages
	public static boolean DEBUG = true;
	public static final String DISALLOW = "Disallow:";
	public static final int MAXSIZE = 2000000; // Max size of file
	public static String path = "";
	public static String CurName = null;
	public static String query;
	public static String[] qs;
	public static boolean isJson;
	// URLs to be searched
	PriorityQueue<ScoredLink> newURLs = new PriorityQueue<ScoredLink>(100,
			new Comparator<ScoredLink>() {
				public int compare(ScoredLink a, ScoredLink b) {
					return b.score - a.score;
				}
			});
	// Known URLs
	// max number of pages to download
	int maxPages;

	// initializes data structures. argv is the command line arguments.

	public void initialize(String[] argv) {
		URL url;

		try {
			url = new URL(argv[0]);
		} catch (MalformedURLException e) {
			System.out.println("Invalid starting URL " + argv[0]);
			return;
		}
		newURLs.add(new ScoredLink(url, 0, ""));

		maxPages = SEARCH_LIMIT;
		if (argv.length > 1) {
			int iPages = Integer.parseInt(argv[1]);
			if (iPages < maxPages)
				maxPages = iPages;
		}
		if (DEBUG) {
			System.out.print("Crawling for " + this.maxPages
					+ " pages relevant to \"" + argv[3] + "\" starting from "
					+ argv[0] + "\n");
		}
		this.query = argv[2];
		qs = query.split(" ");
		isJson = argv[0].contains("sephora") ? true : false;
		this.DEBUG = argv[3].equals("1") ? true : false;
		/*
		 * Behind a firewall set your proxy and port here!
		 */
		Properties props = new Properties(System.getProperties());
		props.put("http.proxySet", "true");
		props.put("http.proxyHost", "webcache-cup");
		props.put("http.proxyPort", "8080");

		Properties newprops = new Properties(props);
		System.setProperties(newprops);
		/**/
	}

	// Check that the robot exclusion protocol does not disallow
	// downloading url.

	public boolean robotSafe(URL url) {
		String strHost = url.getHost();

		// form URL of the robots.txt file
		String strRobot = "http://" + strHost + "/robots.txt";
		URL urlRobot;
		try {
			urlRobot = new URL(strRobot);
		} catch (MalformedURLException e) {
			// something weird is happening, so don't trust it
			return false;
		}

		// if (DEBUG)
		// System.out
		// .println("Checking robot protocol " + urlRobot.toString());
		String strCommands;

		try {
			InputStream urlRobotStream = urlRobot.openStream();
			// read in entire file
			byte b[] = new byte[1000];
			int numRead = urlRobotStream.read(b);
			strCommands = new String(b, 0, numRead);
			while (numRead != -1) {
				numRead = urlRobotStream.read(b);
				if (numRead != -1) {
					String newCommands = new String(b, 0, numRead);
					strCommands += newCommands;
				}
			}
			urlRobotStream.close();
		} catch (IOException e) {
			// if there is no robots.txt file, it is OK to search
			return true;
		}
		// if (DEBUG)
		// System.out.println(strCommands);
		strCommands = process(strCommands);

		// assume that this robots.txt refers to us and
		// search for "Disallow:" commands.
		String strURL = url.getFile();
		int index = 0;
		while ((index = strCommands.indexOf(DISALLOW, index)) != -1) {
			index += DISALLOW.length();
			String strPath = strCommands.substring(index);
			StringTokenizer st = new StringTokenizer(strPath);

			if (!st.hasMoreTokens())
				break;

			String strBadPath = st.nextToken();

			// if the URL starts with a disallowed path, it is not safe
			if (strURL.indexOf(strBadPath) == 0) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Description:
	 * 
	 * @param strCommands
	 * @return
	 */
	public String process(String s) {
		// TODO Auto-generated method stub
		int start = s.indexOf("User-agent: *");
		if (start == -1)
			return s;
		int end = s.indexOf("User-agent", start + 2);
		return end == -1 ? s.substring(start) : s.substring(start, end);
	}

	// adds new URL to the queue. Accept only new URL's that end in
	// htm or html. oldURL is the context, newURLString is the link
	// (either an absolute or a relative URL).

	public void addnewurl(ScoredLink sl)

	{
		System.out.println(sl.url + "\n" + sl.score);
		URL url = sl.url;
		String filename = url.getFile();
		int iSuffix = filename.lastIndexOf("htm");
		if ((iSuffix == filename.length() - 3)
				|| (iSuffix == filename.length() - 4)) {
			newURLs.add(sl);
			if (DEBUG)
				System.out.println("Adding to queue:  " + url.toString()
						+ "\t score=" + sl.score);
		}
	}

	// Download contents of URL

	public String getpage(URL url, int s)

	{
		try {
			// try opening the URL
			URLConnection urlConnection = url.openConnection();
			if (DEBUG)
				System.out.println("Downloading " + url.toString());

			urlConnection.setAllowUserInteraction(false);

			InputStream urlStream = url.openStream();
			// search the input stream for links
			// first, read in the entire URL
			byte b[] = new byte[1000];
			int numRead = urlStream.read(b);
			String content = new String(b, 0, numRead);
			while ((numRead != -1) && (content.length() < MAXSIZE)) {
				numRead = urlStream.read(b);
				if (numRead != -1) {
					String newContent = new String(b, 0, numRead);
					content += newContent;
				}
			}
			// DownloadPages(url, content);
			this.CurName = url.getFile().replace("/", "_");
			return content;

		} catch (IOException e) {
			System.out.println("ERROR: couldn't open URL ");
			return "";
		}
	}

	// Go through page finding links to URLs. A link is signalled
	// by <a href=" ... It ends with a close angle bracket, preceded
	// by a close quote, possibly preceded by a hatch mark (marking a
	// fragment, an internal page marker)
	// 获取一个网页上所有的链接和图片链接
	public void extracLinks(String url) {
		try {
			if (isJson) {
				Parser parser = new Parser(url);
				HasAttributeFilter haf = new HasAttributeFilter("id", "searchResult");
				NodeList list = parser.extractAllNodesThatMatch(haf);
				for (int i = 0; i < list.size(); i++) {
					Node tag = list.elementAt(i);
					System.out.println(tag.toPlainTextString());
				}
			} else {
				Parser parser = new Parser(url);
				// 过滤 <frame> 标签的 filter，用来提取 frame 标签里的 src 属性所、表示的链接
				// NodeFilter frameFilter = new NodeFilter() {
				// public boolean accept(Node node) {
				// if (node.getText().startsWith("frame src=")) {
				// return true;
				// } else {
				// return false;
				// }
				// }
				// };
				// OrFilter 来设置过滤 <a> 标签，<img> 标签和 <frame> 标签，三个标签是 or 的关系
				OrFilter linkFilter = new OrFilter(new NodeClassFilter(
						LinkTag.class), new NodeClassFilter(ImageTag.class));
				// OrFilter linkFilter = new OrFilter(orFilter, frameFilter);
				// 得到所有经过过滤的标签
				NodeList list = parser.extractAllNodesThatMatch(linkFilter);
				for (int i = 0; i < list.size(); i++) {
					Node tag = list.elementAt(i);
					if (tag instanceof LinkTag)// <a> 标签
					{
						LinkTag link = (LinkTag) tag;
						String linkUrl = link.getLink();// url
						String text = link.getLinkText();// 链接文字
						// System.out.println(linkUrl + "**********" + text);
						// if (linkUrl.contains("?") &&
						// !linkUrl.split("\\?")[0].contains(text.replaceAll("[\\p{P}+~$`^=|<>～｀＄＾＋＝｜＜＞￥×]",
						// " ").split(" ")[0])){
						// continue;
						// }
						int linkscore = 0;
						// in the anchor

						for (int j = 0; j < qs.length; j++) {
							if (text.toLowerCase().contains(qs[j])) {
								linkscore += 50;
							}
						}
						if (linkscore > 0) {
							try {
								newURLs.add(new ScoredLink(new URL(linkUrl),
										linkscore, text));
								System.out.println(linkUrl + "\n" + text + "\n"
										+ linkscore);
							} catch (MalformedURLException e) {
								// TODO Auto-generated catch block
							}
						}
					} else if (tag instanceof ImageTag)// <img> 标签
					{
						// ImageTag image = (ImageTag) list.elementAt(i);
						// System.out.print(image.getImageURL() + "********");//
						// 图片地址
						// System.out.println(image.getText());// 图片文字
					} else// <frame> 标签
					{
						// 提取 frame 里 src 属性的链接如 <frame src="test.html"/>
						// String frame = tag.getText();
						// int start = frame.indexOf("src=");
						// frame = frame.substring(start);
						// int end = frame.indexOf(" ");
						// if (end == -1)
						// end = frame.indexOf(">");
						// frame = frame.substring(5, end - 1);
						// System.out.println(frame);
					}
				}
			}
		} catch (ParserException e) {
			e.printStackTrace();
		}
	}

	public void processpage(URL url, String page) {

	}

	// Top-level procedure. Keep popping a url off newURLs, download
	// it, and accumulate new URLs

	public void run(String[] argv)

	{
		initialize(argv);
		for (int i = 0; i < maxPages; i++) {
			ScoredLink sl = newURLs.poll();
			URL url = sl.url;
			if (DEBUG)
				System.out.println("\nSearching " + url.toString()
						+ "\t score=" + sl.score);
			// ScoredLink[] aa = new ScoredLink[1];
			// for (ScoredLink s : newURLs.toArray(aa)){
			// System.out.print(s.url+"\t");
			// }
			if (robotSafe(url)) {
				extracLinks(url.toString());
				if (newURLs.isEmpty())
					break;
			}
		}
		System.out.println("Search complete.");
	}
}