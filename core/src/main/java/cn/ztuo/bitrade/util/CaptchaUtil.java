package cn.ztuo.bitrade.util;

import javax.servlet.http.HttpSession;
import java.util.Enumeration;

public class CaptchaUtil {
	private static final String prefix = "CAPTCHA_";
	
	public static boolean validate(HttpSession session, String pageId, String value){
		String captcha = (String) session.getAttribute(prefix+pageId);
		Enumeration<String> aa = session.getAttributeNames();
		System.out.println(captcha+">>>>>>>>>>>>>>>>>>>>>"+value);
		return captcha != null && captcha.equalsIgnoreCase(value);
	}
}
