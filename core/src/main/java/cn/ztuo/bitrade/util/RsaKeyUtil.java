package cn.ztuo.bitrade.util;

import org.apache.commons.codec.binary.Base64;
//import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;


/** 
 * @Description: RSA工具类
 * @ClassName: RsaKeyUtil 
 * @author nz
 * @date 2019年4月22日 下午3:53:41
 */
public class RsaKeyUtil {


    private RsaKeyUtil() {
    }

//    /**
//     * 初始化密钥对
//     * @Title: generateRSAKeyPair
//     * @param keySize 密钥长度
//     * @return KeyPair    返回类型
//     * @lastModify 2019年4月22日
//     */
//    public static KeyPair generateRSAKeyPair(int keySize) {
//        KeyPairGenerator generator = null;
//        SecureRandom random = new SecureRandom();
//        Security.addProvider(new BouncyCastleProvider());
//        try {
//            generator = KeyPairGenerator.getInstance("RSA", "BC");
//        } catch (GeneralSecurityException e) {
//            e.printStackTrace();
//        }
//        generator.initialize(keySize, random);
//        KeyPair keyPair = generator.generateKeyPair();
//        return keyPair;
//    }


    /** 
     * RSA私钥签名
     * @Title: sign 
     * @param data
     * @param privateKey
     * @return 设定文件 
     * @return byte[]    返回类型 
     * @lastModify 2019年4月22日
     */
    public static byte[] sign(String data, byte[] privateKey) {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey);

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey2 = keyFactory.generatePrivate(pkcs8EncodedKeySpec);

            Signature signature = Signature.getInstance("SHA1WithRSA");
            signature.initSign(privateKey2);
            signature.update(data.getBytes("utf-8"));
            return signature.sign();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 
     * TODO(这里用一句话描述这个方法的作用) 
     * @Title: verify 
     * @param data
     * @param publicKey
     * @param signatureBytes
     * @return 设定文件 
     * @return boolean    返回类型 
     * @lastModify 2019年4月22日
     */
    public static boolean verify(String data, byte[] publicKey, byte[] signatureBytes) {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey);

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey2 = keyFactory.generatePublic(x509EncodedKeySpec);

            Signature signature = Signature.getInstance("SHA1WithRSA");
            signature.initVerify(publicKey2);
            signature.update(data.getBytes("utf-8"));
            return signature.verify(signatureBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 后台测试签名的时候 要和前台保持一致，所以需要将结果转换
    public static String bytesToHexString(byte[] bytes) {
        StringBuilder string = new StringBuilder();
        for (byte b : bytes) {
            String hexString = Integer.toHexString(0x00FF & b);
            string.append(hexString.length() == 1 ? "0" + hexString : hexString);
        }
        return string.toString();
    }

    // 前台的签名结果是将byte中的一些 负数转换成了正数，
    // 但是后台验证的方法需要的又必须是转换之前的
    public static byte[] hexStringToBytes(String data) {
        int k = 0;
        byte[] results = new byte[data.length() / 2];
        for (int i = 0; i + 1 < data.length(); i += 2, k++) {
            results[k] = (byte) (Character.digit(data.charAt(i), 16) << 4);
            results[k] += (byte) (Character.digit(data.charAt(i + 1), 16));
        }
        return results;
    }


    public static void main(String[] args) {
        String data = "amount=1&currency=HNB&fromAddress=1&toAddress=1&userId=&verifyCode=1";
//        KeyPair keyPair = generateRSAKeyPair(1024);
//        RSAPublicKey publicKey =(RSAPublicKey) keyPair.getPublic();
//        RSAPrivateKey rsaPublicKey = (RSAPrivateKey) keyPair.getPrivate();
//        System.out.println("publicKey->\n" + publicKey.getModulus().toString());
//        System.out.println("publicKey->\n" + publicKey.getPublicExponent());
//        System.out.println("privateKey->\n" + rsaPublicKey.getModulus());
//        System.out.println("privateKey->\n" + rsaPublicKey.getPrivateExponent());
        try {
            byte[] signautre = sign(data, decryptBASE64("test"));
            String signatureStr = encryptBASE64(signautre);
//            signatureStr = "Cy7vuldt0P81Byr7GYq1Se2r/RlO4O91hucdV4abJWVop+oZ4LSSIQYrp5ygCNU9HQ8Z6gyHuBI07kyoayXAwFszAkAHYKBllv5kc6FeeJYgVPTl96NOW6Ez9aE7MHEF1TGgnJlPUZadoYLZZEou5gW8D1fP9/RTiTl8NdMsbeI=";
            System.out.println("signautre->" + signatureStr);
            byte[] signatureBytes = decryptBASE64(signatureStr);

            boolean b = verify(data, decryptBASE64("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAh08dxdKjV7jcu9L/1HzmVoo7T/+8ItY9RfD827+9oixezutFOdpIiZRAjh93eyB2c5n2nv7zN8RKUp3ku9ZFYsqyulPTJt6PB8xHjZUz35AP7NYPEXj3XsiVCmdNE1fAeFLbgpR3CbRiAxsste0fLvoJvtEWGMSdpejF0OVMGQCe6Y6UbOLzMMrkKtjxVCxouIYF0ljkDyXA3bkZxTOWtMHootvCsMjTAP8gOJbQHZArJKtRZcmyTaqcN0DB667Kn6EllnqiUfDmqKjam4QvhzGFBzs+PjIsOCpHp+9NAy+E8utMf+UbXQ+Ai/2tLHFV5zHvEw9CxDrL9R+Qxxgd5wIDAQAB"), signatureBytes);
            System.out.print("result->" + b);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
	public static byte[] decryptBASE64(String key) {
		return Base64.decodeBase64(key);
	}

	public static String encryptBASE64(byte[] bytes) {
		return Base64.encodeBase64String(bytes);
	}
	
	
	/**
	 * 使用模和指数生成RSA公钥
	 * 注意：【此代码用了默认补位方式，为RSA/None/PKCS1Padding，不同JDK默认的补位方式可能不同，如Android默认是RSA/None/NoPadding】
	 * 
	 * @param modulus 模
	 * @param pubexponent 指数
	 * @return
	 */
	public static String getPublicKey(String modulus, String pubexponent) {
		try {
			BigInteger b1 = new BigInteger(modulus);
			BigInteger b2 = new BigInteger(pubexponent);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			RSAPublicKeySpec keySpec = new RSAPublicKeySpec(b1, b2);
			return bytesToHexString(((RSAPublicKey) keyFactory.generatePublic(keySpec)).getEncoded());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 使用模和指数生成RSA私钥
	 * 注意：【此代码用了默认补位方式，为RSA/None/PKCS1Padding，不同JDK默认的补位方式可能不同，如Android默认是RSA/None/NoPadding】
	 * 
	 * @param modulus 模
	 * @param priexponent 指数
	 * @return
	 */
	public static String getPrivateKey(String modulus, String priexponent) {
		try {
			BigInteger b1 = new BigInteger(modulus);
			BigInteger b2 = new BigInteger(priexponent);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(b1, b2);
			return bytesToHexString(((RSAPrivateKey) keyFactory.generatePrivate(keySpec)).getEncoded());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
