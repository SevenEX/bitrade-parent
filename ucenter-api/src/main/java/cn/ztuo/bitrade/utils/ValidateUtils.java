package cn.ztuo.bitrade.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidateUtils {
    final static String passswordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[\\s\\S]{6,20}$";  //^(?=.*?[a-zA-Z])(?=.*?[0-9])[a-zA-Z0-9]{6,20}$
    public static Boolean validatePassword(String pwd) {
        Pattern r = Pattern.compile(passswordPattern);
        Matcher m = r.matcher(pwd);
        return m.matches();
    }

}
