package com.leyou.upload.service.impl;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.upload.config.UploadProperties;
import com.leyou.upload.service.UploadService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Service
@EnableConfigurationProperties(UploadProperties.class)
public class UploadServiceImpl implements UploadService {

    @Autowired
    private FastFileStorageClient storageClient;

    @Autowired
    private UploadProperties prop;

    @Override
    public String upload(MultipartFile file) {
        try {
            //1.图片信息校验
            //1,校验文件类型
            String type = file.getContentType();
            //用配置文件注入的形式
            if (!prop.getAllowTypes().contains(type)) {
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            //2,校验图片内容
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (null == image) {
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            //2.将图片上传到FastDFS
            //2.1获取文件后缀名
            String extension = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");
            //2.2上传
            StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), extension, null);
            //2.3返回完整的路径
            return prop.getBaseUrl() + storePath.getFullPath();
        } catch (IOException e) {
            throw new LyException(ExceptionEnum.FILE_UPLOAD_ERROR);
        }
    }
}
