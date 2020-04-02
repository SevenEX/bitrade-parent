package cn.ztuo.bitrade.util;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;


public class RSATest {

//	公钥
//	      publicKey=
	//MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJfN74svxDerQxvO7EBWtDbsNz8rEZ/w4r8O2hklIEKkrvgx24wXYoH8afL+TSOyhq5glpyEdPqH1zFvWrECQDUCAwEAAQ==
//	      私钥
//	      privateKey=
	//MIIBVQIBADANBgkqhkiG9w0BAQEFAASCAT8wggE7AgEAAkEAl83viy/EN6tDG87sQFa0Nuw3PysRn/Divw7aGSUgQqSu+DHbjBdigfxp8v5NI7KGrmCWnIR0+ofXMW9asQJANQIDAQABAkB/7YdKN5XXCBfEzotf6PF0O3qPXD89FyYutlhrylK2dX7VXKjqjAUw51By+LtYKpugzvg7dH5qGKxA7TPqR/JBAiEA1NY563c9VKs4/K9/zjgjXA1U+sBvCEhCV7w/m9kyqaUCIQC2lxzf8yiRK3E+BRf2cfCcD5arr+WsQusmkvrQ+63XUQIhAK29OoamTaBiLSDIOnIijWrFT0tp7rk6Ez3/Y4VbJeopAiA0L45uJ4ZBr4PnMPd/VWNqhz4OBfEDwgCJftq23if6cQIhAJm9g69DWxBMuJPcSQbKU/iA81/0aq4bmcUlFoa4JPqH

	public static final String KEY_ALGORITHM="RSA";  
	/** 
     * 密钥长度，DH算法的默认密钥长度是1024 
     * 密钥长度必须是64的倍数，在512到65536位之间 
     * */  
    private static final int KEY_SIZE=512;  
	//公钥  
    private static final String PUBLIC_KEY="RSAPublicKey";  
    //私钥  
    private static final String PRIVATE_KEY="RSAPrivateKey"; 
    /** 
    * 初始化密钥对 
    * @return Map 甲方密钥的Map 
    * */  
   public static Map<String,Object> initKey() throws Exception{  
       //实例化密钥生成器  
       KeyPairGenerator keyPairGenerator=KeyPairGenerator.getInstance(KEY_ALGORITHM);  
       //初始化密钥生成器  
       keyPairGenerator.initialize(KEY_SIZE);  
       //生成密钥对  
       KeyPair keyPair=keyPairGenerator.generateKeyPair();  
       //甲方公钥  
       RSAPublicKey publicKey=(RSAPublicKey) keyPair.getPublic();  
       //甲方私钥  
       RSAPrivateKey privateKey=(RSAPrivateKey) keyPair.getPrivate();  
       //将密钥存储在map中  
       Map<String,Object> keyMap=new HashMap<String,Object>();  
       keyMap.put(PUBLIC_KEY, publicKey);  
       keyMap.put(PRIVATE_KEY, privateKey);  
       return keyMap;  
   }
//	public static void main(String[] args) {
//		//String data1="342529";//服务密码
//		String data1="100865";//服务密码
//
//		String data2="13940417085";//短信随机密码
//		String data3="152122199106236628";//证件号码
//		String data4="899734";//证件姓名
//		String data5="辽宁省沈阳市浑南新区高歌路1号";//证件地址
//		String outData="";
//		String decryptData="";
//		List<String> list= new ArrayList<String>();
//		list.add(data1);
//		list.add(data2);
//		list.add(data3);
//		list.add(data4);
//		list.add(data5);
////		Map<String, Object> keyMap=null;
////		try {
////			keyMap = initKey();
////		} catch (Exception e1) {
////			e1.printStackTrace();
////		}
//        //公钥
//		String publicKey="MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJfN74svxDerQxvO7EBWtDbsNz8rEZ/w4r8O2hklIEKkrvgx24wXYoH8afL+TSOyhq5glpyEdPqH1zFvWrECQDUCAwEAAQ==";
//        //String publicKey=Base64.encodeBase64String(RSAUtil.getPublicKey(keyMap));
//        System.out.println("公钥="+publicKey);
//        //私钥
//        String privateKey="MIIBVQIBADANBgkqhkiG9w0BAQEFAASCAT8wggE7AgEAAkEAl83viy/EN6tDG87sQFa0Nuw3PysRn/Divw7aGSUgQqSu+DHbjBdigfxp8v5NI7KGrmCWnIR0+ofXMW9asQJANQIDAQABAkB/7YdKN5XXCBfEzotf6PF0O3qPXD89FyYutlhrylK2dX7VXKjqjAUw51By+LtYKpugzvg7dH5qGKxA7TPqR/JBAiEA1NY563c9VKs4/K9/zjgjXA1U+sBvCEhCV7w/m9kyqaUCIQC2lxzf8yiRK3E+BRf2cfCcD5arr+WsQusmkvrQ+63XUQIhAK29OoamTaBiLSDIOnIijWrFT0tp7rk6Ez3/Y4VbJeopAiA0L45uJ4ZBr4PnMPd/VWNqhz4OBfEDwgCJftq23if6cQIhAJm9g69DWxBMuJPcSQbKU/iA81/0aq4bmcUlFoa4JPqH";
//        //String privateKey= Base64.encodeBase64String(RSAUtil.getPrivateKey(keyMap));
//        System.out.println("私钥="+privateKey);
//        System.out.println("序号0=服务密码，序号1=短信随机密码，序号2=证件号码，序号3=证件姓名，序号4=证件地址");
//        for(int i=0;i<list.size();i++){
//        	byte[] data=list.get(i).getBytes();
//    		try {
//    			//公钥加密
//    			outData=RSAUtil.encryptByPublicKey(data,publicKey);
//    			System.out.println("序号i="+i+",加密前="+list.get(i)+",加密后="+outData);
//    		} catch (Exception e) {
//    			e.printStackTrace();
//    		}
//    		//私钥解密
//    		try {
//    			decryptData=new String(RSAUtil.decryptByPrivateKey(outData,privateKey));
//    			System.out.println("序号i="+i+",解密前="+outData+",解密后="+decryptData);
//    		} catch (Exception e) {
//    			e.printStackTrace();
//    		}
//        }
//	}

}
