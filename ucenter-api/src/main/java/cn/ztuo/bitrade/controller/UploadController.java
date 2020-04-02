package cn.ztuo.bitrade.controller;

import cn.ztuo.bitrade.config.AliyunConfig;
import cn.ztuo.bitrade.service.LocaleMessageSourceService;
import cn.ztuo.bitrade.util.GeneratorUtil;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.MinioUtil;
import cn.ztuo.bitrade.util.UploadFileUtil;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.CannedAccessControlList;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@Slf4j
@Api(tags = "文件/图片上传")
public class UploadController {
    private String allowedFormat = ".jpg,.gif,.png,.jpeg";
    private String allowedVideo = ".mp4,.avi";
    @Autowired
    private AliyunConfig aliyunConfig;
    @Autowired
    private LocaleMessageSourceService sourceService;
    @Value("${person.promote.host:}")
    private String host;

    @RequestMapping(value = "upload/oss/image", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation("上传图片")
    public MessageResult uploadOssImage(HttpServletRequest request, HttpServletResponse response,
                                        @RequestParam("file") MultipartFile file) throws IOException {
        log.info(request.getSession().getServletContext().getResource("/").toString());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        Assert.isTrue(ServletFileUpload.isMultipartContent(request), sourceService.getMessage("FORM_FORMAT_ERROR"));
        Assert.isTrue(file != null, sourceService.getMessage("NOT_FIND_FILE"));
        String fileType = UploadFileUtil.getFileType(file.getInputStream());
        log.info("fileType=" + fileType);
        String directory = new SimpleDateFormat("yyyy/MM/dd/").format(new Date());
        OSS ossClient = new OSSClientBuilder().build(aliyunConfig.getOssEndpoint(), aliyunConfig.getAccessKeyId(), aliyunConfig.getAccessKeySecret());
        try {
            String fileName = file.getOriginalFilename();
            String suffix = fileName.substring(fileName.lastIndexOf("."), fileName.length());
            if (!allowedFormat.contains(suffix.trim().toLowerCase())) {
                return MessageResult.error(sourceService.getMessage("FORMAT_NOT_SUPPORT"));
            }
            if (fileType == null || !allowedFormat.contains(fileType.trim().toLowerCase())) {
                return MessageResult.error(sourceService.getMessage("FORMAT_NOT_SUPPORT"));
            }
            String key = directory + GeneratorUtil.getUUID() + suffix;
            //压缩文件
            String path = request.getSession().getServletContext().getRealPath("/") + "upload/" + file.getOriginalFilename();
            File tempFile = new File(path);
            FileUtils.copyInputStreamToFile(file.getInputStream(), tempFile);
            log.info("=================压缩前" + tempFile.length());
            UploadFileUtil.zipWidthHeightImageFile(tempFile, tempFile, 425, 638, 0.7f);
            log.info("=================压缩后" + tempFile.length());
            ossClient.putObject(aliyunConfig.getOssBucketName(), key, file.getInputStream());
            String uri = aliyunConfig.toUrl(key);
            MessageResult mr = new MessageResult(0, sourceService.getMessage("UPLOAD_SUCCESS"));
            mr.setData(uri);
            return mr;
        } catch (OSSException oe) {
            return MessageResult.error(500, oe.getErrorMessage());
        } catch (ClientException ce) {
            return MessageResult.error(500, ce.getErrorMessage());
        } catch (Throwable e) {
            e.printStackTrace();
            return MessageResult.error(500, sourceService.getMessage("SYSTEM_ERROR"));
        } finally {
            ossClient.shutdown();
        }
    }


    /**
     * 实名认证视频上传
     * @param request
     * @param response
     * @param file
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "upload/video", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation("上传视频")
    public MessageResult uploadVideo(HttpServletRequest request, HttpServletResponse response,
                                     @RequestParam("file") MultipartFile file) throws IOException {
        log.info(request.getSession().getServletContext().getResource("/").toString()+"==========实名认证视频上传");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        Assert.isTrue(ServletFileUpload.isMultipartContent(request), sourceService.getMessage("FORM_FORMAT_ERROR"));
        Assert.isTrue(file != null, sourceService.getMessage("NOT_FIND_FILE"));
        log.info("该文件的文件流转为流类型>>>>>>>>>>>"+UploadFileUtil.getFileHeader(file.getInputStream()));
        //文件大小判断
        long fileSize = file.getSize();
        long fileSizeMB = fileSize/(1024*1024);
        log.info("==============文件大小："+fileSize);
        if ( fileSizeMB > 20){
            return MessageResult.error(sourceService.getMessage("File.max.size"));
        }
        String directory = new SimpleDateFormat("yyyy/MM/dd/").format(new Date());
        OSS ossClient = new OSSClientBuilder().build(aliyunConfig.getOssEndpoint(), aliyunConfig.getAccessKeyId(), aliyunConfig.getAccessKeySecret());
        try {
            String fileName = file.getOriginalFilename();
            log.info("===========fileName:"+fileName);
            String suffix = fileName.substring(fileName.lastIndexOf("."), fileName.length());
            log.info("suffix="+suffix);
            String key = directory + GeneratorUtil.getUUID() + suffix;
            log.info("key:"+key);
            ossClient.putObject(aliyunConfig.getOssBucketName(), key, file.getInputStream());
            String uri = aliyunConfig.toUrl(key);
            log.info(">>>>>>>>>>上传成功>>>>>>>>>>>>>");
            MessageResult mr = new MessageResult(0, sourceService.getMessage("UPLOAD_SUCCESS"));
            mr.setData(uri);
            return mr;
        } catch (OSSException oe) {
            return MessageResult.error(500, oe.getErrorMessage());
        } catch (ClientException ce) {
            System.out.println("Error Message: " + ce.getMessage());
            return MessageResult.error(500, ce.getErrorMessage());
        } catch (Throwable e) {
            e.printStackTrace();
            return MessageResult.error(500, sourceService.getMessage("SYSTEM_ERROR"));
        } finally {
            ossClient.shutdown();
        }

    }


    @RequestMapping(value = "upload/local/image", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation("图片上传到本地服务器")
    public MessageResult uploadLocalImage(HttpServletRequest request, HttpServletResponse response,
                                          @RequestParam("file") MultipartFile file) throws IOException {
        log.info(request.getSession().getServletContext().getResource("/").toString());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        Assert.isTrue(ServletFileUpload.isMultipartContent(request), sourceService.getMessage("FORM_FORMAT_ERROR"));
        Assert.isTrue(file != null, sourceService.getMessage("NOT_FIND_FILE"));
        //验证文件类型
        String fileName = file.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."), fileName.length());
        if (!allowedFormat.contains(suffix.trim().toLowerCase())) {
            return MessageResult.error(sourceService.getMessage("FORMAT_NOT_SUPPORT"));
        }
        String result = UploadFileUtil.uploadFile(file, fileName);
        if (result != null) {
            MessageResult mr = new MessageResult(0, sourceService.getMessage("UPLOAD_SUCCESS"));
            mr.setData(host+result);
            return mr;
        } else {
            MessageResult mr = new MessageResult(0, sourceService.getMessage("FAILED_TO_WRITE"));
            mr.setData(result);
            return mr;
        }
    }


    /**
     * 上传base64处理后的图片
     *
     * @param base64Data
     * @return
     */
    @RequestMapping(value = "/upload/oss/base64", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation("上传base64处理后的图片")
    public MessageResult base64UpLoad(
            @RequestParam String base64Data,
            @RequestParam(value = "oss", defaultValue = "true", required = false) Boolean oss) {
        log.info("oss == " + oss);
        MessageResult result = new MessageResult();
        String uri;
        try {
            log.debug("上传文件的数据：" + base64Data);
            String dataPrix = "";
            String data = "";
            if (base64Data == null || "".equals(base64Data)) {
                throw new Exception(sourceService.getMessage("NOT_FIND_FILE"));
            } else {
                String[] d = base64Data.split("base64,");
                if (d != null && d.length == 2) {
                    dataPrix = d[0];
                    data = d[1];
                } else {
                    throw new Exception(sourceService.getMessage("DATA_ILLEGAL"));
                }
            }
            log.debug("对数据进行解析，获取文件名和流数据");
            String suffix = "";
            if ("data:image/jpeg;".equalsIgnoreCase(dataPrix)) {//data:image/jpeg;base64,base64编码的jpeg图片数据
                suffix = ".jpg";
            } else if ("data:image/x-icon;".equalsIgnoreCase(dataPrix)) {//data:image/x-icon;base64,base64编码的icon图片数据
                suffix = ".ico";
            } else if ("data:image/gif;".equalsIgnoreCase(dataPrix)) {//data:image/gif;base64,base64编码的gif图片数据
                suffix = ".gif";
            } else if ("data:image/png;".equalsIgnoreCase(dataPrix)) {//data:image/png;base64,base64编码的png图片数据
                suffix = ".png";
            } else {
                throw new Exception(sourceService.getMessage("FORMAT_NOT_SUPPORT"));
            }
            String directory = new SimpleDateFormat("yyyy/MM/dd/").format(new Date());
            String key = directory + GeneratorUtil.getUUID() + suffix;

            //因为BASE64Decoder的jar问题，此处使用spring框架提供的工具包
            byte[] bs = Base64Utils.decodeFromString(data);

            if (oss == true) {
                //阿里云上传
                OSS ossClient = new OSSClientBuilder().build(aliyunConfig.getOssEndpoint(), aliyunConfig.getAccessKeyId(), aliyunConfig.getAccessKeySecret());
                try {
                    //使用apache提供的工具类操作流
                    InputStream is = new ByteArrayInputStream(bs);
                    //FileUtils.writeByteArrayToFile(new File(Global.getConfig(UPLOAD_FILE_PAHT), tempFileName), bs);
                    ossClient.putObject(aliyunConfig.getOssBucketName(), key, is);
                    uri = aliyunConfig.toUrl(key);
                    result.setData(uri);
                    result.setMessage(sourceService.getMessage("UPLOAD_SUCCESS"));
                    log.debug("上传成功,accessKey:{}", key);
                    return result;
                } catch (Exception ee) {
                    log.info(ee.getMessage());
                    throw new Exception(sourceService.getMessage("FAILED_TO_WRITE"));
                }
            } else {
                //本地上传
                uri = UploadFileUtil.uploadFileByBytes(bs, GeneratorUtil.getUUID());
                result.setData(uri);
            }


        } catch (Exception e) {
            log.debug("上传失败," + e.getMessage());

            result.setCode(500);
            result.setMessage(e.getMessage());
        }

        return result;
    }
    /**
     * 上传
     * @param request
     * @param response
     * @param file
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "upload/file", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation("上传文件（废弃）")
    public MessageResult upload(HttpServletRequest request, HttpServletResponse response,
                                        @RequestParam("file") MultipartFile file) throws IOException {
        log.info(request.getSession().getServletContext().getResource("/").toString());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
        Assert.isTrue(file != null, sourceService.getMessage("NOT_FIND_FILE"));
        Assert.isTrue(ServletFileUpload.isMultipartContent(request), sourceService.getMessage("FORM_FORMAT_ERROR"));

        String directory = new SimpleDateFormat("yyyy/MM/dd/").format(new Date());
        try {
            String fileName = file.getOriginalFilename();
            String suffix = fileName.substring(fileName.lastIndexOf("."), fileName.length());
            String key = directory + GeneratorUtil.getUUID() + suffix;
            String uri = MinioUtil.upload(file.getBytes(),key);
            MessageResult mr = new MessageResult(0, sourceService.getMessage("UPLOAD_SUCCESS"));
            mr.setData(uri);
            return mr;
        } catch (Throwable e) {
            e.printStackTrace();
            return MessageResult.error(500, sourceService.getMessage("SYSTEM_ERROR"));
        }
    }

    public  static void main(String[] arg){
        List<File> files = getFiles("L:\\weichatfiles\\WeChat Files\\LV1103164484\\FileStorage\\File\\2019-12\\svg");

        OSS ossClient = new OSSClientBuilder().build("oss-ap-northeast-1.aliyuncs.com", "LTAI4FnPqtBGgrfBgGVf54os", "5ACPZwPJYm68JvELl3tSsU9qMgwhgE");
        try {
            for(File file : files){
                String fileName = file.getName();
                ossClient.putObject("test", "upload/svg/" +fileName, file);
                ossClient.setObjectAcl("test","upload/svg/" +fileName, CannedAccessControlList.PublicRead);
                String uri = String.format("%s://%s.%s/%s", "http","test","oss-ap-northeast-1.aliyuncs.com","upload/svg/" +fileName);
                System.out.println(uri);
            }

        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            ossClient.shutdown();
        }
    }

    public static List<File> getFiles(String path){
        File root = new File(path);
        List<File> files = new ArrayList<File>();
        if(!root.isDirectory()){
            files.add(root);
        }else{
            File[] subFiles = root.listFiles();
            for(File f : subFiles){
                files.addAll(getFiles(f.getAbsolutePath()));
            }
        }
        return files;
    }

}
