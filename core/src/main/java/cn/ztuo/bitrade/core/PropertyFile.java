package cn.ztuo.bitrade.core;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Properties;

/**
 * @description: PropertyFile
 * @author: MrGao
 * @create: 2019/07/04 14:49
 */
public class PropertyFile {
    private Properties properties;
    private InputStream file;
    private String configFilePath;

    public PropertyFile() {
        this("config.properties");
    }

    public PropertyFile(String propertyFileName) {
        try {
            this.configFilePath = URLDecoder.decode(this.getClass().getResource("/").getPath() + propertyFileName, "UTF-8");
            this.properties = new Properties();
            this.file = new FileInputStream(this.configFilePath);
            this.properties.load(this.file);
            return;
        } catch (IOException var15) {
            try {
                this.file.close();
            } catch (IOException var14) {
                var14.printStackTrace();
                return;
            }
        } finally {
            try {
                this.file.close();
            } catch (IOException var13) {
                var13.printStackTrace();
            }

        }

    }

    public PropertyFile(String propertyFilePath, String propertyFileName) {
        try {
            this.configFilePath = URLDecoder.decode(propertyFilePath + propertyFileName, "UTF-8");
            this.properties = new Properties();
            this.file = new FileInputStream(this.configFilePath);
            this.properties.load(this.file);
            return;
        } catch (IOException var16) {
            try {
                this.file.close();
            } catch (IOException var15) {
                var15.printStackTrace();
                return;
            }
        } finally {
            try {
                this.file.close();
            } catch (IOException var14) {
                var14.printStackTrace();
            }

        }

    }

    public String read(String key) {
        return this.properties.getProperty(key);
    }

    public int readInt(String key, int value) {
        return Convert.strToInt(this.properties.getProperty(key), value);
    }

    public String readString(String key, String value) {
        return Convert.strToStr(this.properties.getProperty(key), value);
    }

    public void write(String key, String value) {
        FileOutputStream localFileOutputStream = null;

        try {
            this.properties.setProperty(key, value);
            localFileOutputStream = new FileOutputStream(this.configFilePath);
            this.properties.store(localFileOutputStream, "");
            localFileOutputStream.flush();
            return;
        } catch (Exception var17) {
            try {
                localFileOutputStream.close();
                return;
            } catch (IOException var16) {
                var16.printStackTrace();
            }
        } finally {
            try {
                localFileOutputStream.close();
            } catch (IOException var15) {
                var15.printStackTrace();
            }

        }

    }
}
