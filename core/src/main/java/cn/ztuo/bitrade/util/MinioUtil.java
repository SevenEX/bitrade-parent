package cn.ztuo.bitrade.util;

import eu.medsea.mimeutil.MimeUtil;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;

@Configuration
@Slf4j
public class MinioUtil {


    private static String minio_url;

    private static String minioAccesskey;

    public static String minioSecretKey;

    public static String minioBucket;

    private static String minio_base_url;


    static {
        minio_url = "http://test:8008";
        minioAccesskey = "test";
        minioSecretKey = "test";
        minioBucket = "bitrade-parent-test";
    }


    @Value("${minio_url}")
    public void setMinio_url(String minio_url) {
        MinioUtil.minio_url = minio_url;
    }

    @Value("${minioAccesskey}")
    public void setMinioAccesskey(String minioAccesskey) {
        MinioUtil.minioAccesskey = minioAccesskey;
    }

    @Value("${minioSecretKey}")
    public void setMinioSecretKey(String minioSecretKey) {
        MinioUtil.minioSecretKey = minioSecretKey;
    }

    @Value("${minioBucket}")
    public void setMinioBucket(String minioBucket) {
        MinioUtil.minioBucket = minioBucket;
    }

    @Value("${minio_base_url}")
    public void setMinio_base_url(String minio_base_url) {
        MinioUtil.minio_base_url = minio_base_url;
    }

    /**
     * @Title: upload
     * @Description:上传主功能
     * @return
     * @throws Exception
     */
    public static String upload(byte[] uploadBytes, String key)  {
        try {
            Collection<?> mimeTypes = MimeUtil.getMimeTypes(uploadBytes);
            MinioClient minioClient = new MinioClient(minio_url, minioAccesskey, minioSecretKey);
            InputStream inputStream = new ByteArrayInputStream(uploadBytes);
            minioClient.putObject(minioBucket, key, inputStream, uploadBytes.length, mimeTypes.toString());
            log.info("Content-Type==="+mimeTypes.toString());
            return minio_base_url+"/"+minioBucket+"/"+key;
        } catch (Exception e) {
            return null;
        }
    }

    public   static void main(String[] arg){
        upload(new byte[1024], "a.jpg");
    }
}
