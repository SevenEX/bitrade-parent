package cn.ztuo.bitrade.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.Cipher;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//非对称加密
@Slf4j
public class RSAUtil {
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
	    /** 
	     * 私钥加密 
	     * @param data 待加密数据
	     * @param privKey 密钥
	     * @return byte[] 加密数据 
	     * */  
	    public static String encryptByPrivateKey(byte[] data,String privKey) throws Exception {
	    	try {
				byte[] key = Base64.decodeBase64(privKey);
				//取得私钥  
				PKCS8EncodedKeySpec pkcs8KeySpec=new PKCS8EncodedKeySpec(key);  
				KeyFactory keyFactory=KeyFactory.getInstance(KEY_ALGORITHM);  
				//生成私钥  
				PrivateKey privateKey =keyFactory.generatePrivate(pkcs8KeySpec);
				//数据加密  
				Cipher cipher=Cipher.getInstance(keyFactory.getAlgorithm());  
				cipher.init(Cipher.ENCRYPT_MODE, privateKey);  
				byte[] b = cipher.doFinal(data);
				return Base64.encodeBase64String(b);
			} catch (Exception e) {
	    		log.info(">>>>>>>加密数据异常>>>>>"+e);
				throw new Exception(e.getMessage());
			}
	    }  
	    /** 
	     * 公钥加密 
	     * @param data 待加密数据
	     * @param publicKey 密钥
	     * @return byte[] 加密数据 
	     * */  
	    public static String encryptByPublicKey(byte[] data,String publicKey) throws Exception{
	    	try {
				byte[] key = Base64.decodeBase64(publicKey);
				//实例化密钥工厂  
				KeyFactory keyFactory=KeyFactory.getInstance(KEY_ALGORITHM);  
				//初始化公钥  
				//密钥材料转换  
				X509EncodedKeySpec x509KeySpec=new X509EncodedKeySpec(key);  
				//产生公钥  
				PublicKey pubKey=keyFactory.generatePublic(x509KeySpec);  
				  
				//数据加密  
				Cipher cipher=Cipher.getInstance(keyFactory.getAlgorithm());  
				cipher.init(Cipher.ENCRYPT_MODE, pubKey);  
				byte[] data2 =  cipher.doFinal(data); 
				return  Base64.encodeBase64String(data2);
			} catch (Exception e) {
				log.info(">>>>>>>加密数据异常>>>>>"+e);
				throw new Exception(e);
			}
	    }  
	    /** 
	     * 私钥解密 
	     * @param content 待解密数据
	     * @param privKey 密钥
	     * @return byte[] 解密数据 
	     * */  
	    public static byte[] decryptByPrivateKey(String content,String privKey) throws Exception {
	    	try {
				byte[] key = Base64.decodeBase64(privKey);
				byte[] data = Base64.decodeBase64(content);
				//取得私钥  
				PKCS8EncodedKeySpec pkcs8KeySpec=new PKCS8EncodedKeySpec(key);  
				KeyFactory keyFactory=KeyFactory.getInstance(KEY_ALGORITHM);  
				//生成私钥  
				PrivateKey privateKey=keyFactory.generatePrivate(pkcs8KeySpec);  
				//数据解密  
				Cipher cipher=Cipher.getInstance(keyFactory.getAlgorithm());  
				cipher.init(Cipher.DECRYPT_MODE, privateKey);  
				return cipher.doFinal(data);
			} catch (Exception e) {
				log.info(">>>>>>>加密数据异常>>>>>"+e);
				throw new Exception(e);
			} 
	    }  
	    /** 
	     * 公钥解密 
	     * @param content 待解密数据
	     * @param publicKey 密钥
	     * @return byte[] 解密数据 
	     * */  
	    public static byte[] decryptByPublicKey(String content,String publicKey) throws Exception{
	    	try {
				byte[] key = Base64.decodeBase64(publicKey);
				byte[] data = Base64.decodeBase64(content);
				//实例化密钥工厂  
				KeyFactory keyFactory=KeyFactory.getInstance(KEY_ALGORITHM);  
				//初始化公钥  
				//密钥材料转换  
				X509EncodedKeySpec x509KeySpec=new X509EncodedKeySpec(key);  
				//产生公钥  
				PublicKey pubKey=keyFactory.generatePublic(x509KeySpec);  
				//数据解密  
				Cipher cipher=Cipher.getInstance(keyFactory.getAlgorithm());  
				cipher.init(Cipher.DECRYPT_MODE, pubKey);  
				return cipher.doFinal(data);
			} catch (Exception e) {
				log.info(">>>>>>>加密数据异常>>>>>"+e);
				throw new Exception(e);
			}  
	    }  
	    /** 
	     * 取得私钥 
	     * @param keyMap 密钥map 
	     * @return byte[] 私钥 
	     * */  
	    public static byte[] getPrivateKey(Map<String,Object> keyMap){  
	        Key key=(Key)keyMap.get(PRIVATE_KEY);  
	        return key.getEncoded();  
	    }  
	    /** 
	     * 取得公钥 
	     * @param keyMap 密钥map 
	     * @return byte[] 公钥 
	     * */  
	    public static byte[] getPublicKey(Map<String,Object> keyMap){  
	        Key key=(Key) keyMap.get(PUBLIC_KEY);  
	        return key.getEncoded();  
	    }
	    /**
	     * @param args
	     * @throws Exception
	     */
	    public static void main(String[] args) throws Exception {

	    	System.out.println(new Date(1531997063000L));
	    	System.out.println(new Date(1532083463521L));
	        //初始化密钥
	        //生成密钥对
	        Map<String,Object> keyMap=RSAUtil.initKey();
	        //公钥
//	        byte[] publicKey=RSAUtil.getPublicKey(keyMap);
	        String publicKey=Base64.encodeBase64String(RSAUtil.getPublicKey(keyMap));
	        //私钥
//	        byte[] privateKey=RSAUtil.getPrivateKey(keyMap);
	        String privateKey= Base64.encodeBase64String(RSAUtil.getPrivateKey(keyMap));
	        System.out.println("公钥："+publicKey );
	        System.out.println("私钥："+privateKey);

	        System.out.println("================密钥对构造完毕,甲方将私钥公布给乙方，开始进行加密数据的传输=============");
	        String str="RSA密码交换算法";
	        System.out.println("===========甲方向乙方发送加密数据==============");
	        System.out.println("原文:"+str);
	        //甲方进行数据的加密 公钥
	        String code1=RSAUtil.encryptByPublicKey(str.getBytes(), publicKey);
	        System.out.println("加密后的数据："+code1);
	        System.out.println("===========乙方使用甲方提供的公钥对数据进行解密==============");
	        //乙方进行数据的解密
	        byte[] decode1=RSAUtil.decryptByPrivateKey(code1, privateKey);
	        System.out.println("乙方解密后的数据："+new String(decode1)+"/n/n");

	        System.out.println("===========反向进行操作，乙方向甲方发送数据==============/n/n");

	        str="乙方向甲方发送数据RSA算法";

	        System.out.println("原文:"+str);

	        //乙方使用si钥对数据进行加密
	        String code2=RSAUtil.encryptByPrivateKey(str.getBytes(), privateKey);
	        System.out.println("===========乙方使用公钥对数据进行加密==============");
	        System.out.println("加密后的数据："+code2);
	        System.out.println("=============乙方将数据传送给甲方======================");
	        System.out.println("===========甲方使用私钥对数据进行解密==============");

	        //甲方使用公钥对数据进行解密
	        byte[] decode2=RSAUtil.decryptByPublicKey(code2, publicKey);

	        System.out.println("甲方解密后的数据："+new String(decode2));
	    }
}
