/**
 * @author Hansi Mou
 * @date Mar 7, 2016
 * @version 1.0
 */

/**
 * @author Hansi Mou
 *
 * Mar 7, 2016
 */
public class HrefText {
	String href = "";
	String text = "";
	public HrefText(String a, String b){
		this.href = a;
		this.text = b;
	}
	public String print(){
		if (href.length() == 0){
			return ""+text;
		}
		else{
			return href+"---"+text;
		}
	}
}
