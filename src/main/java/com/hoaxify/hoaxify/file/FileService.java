package com.hoaxify.hoaxify.file;

import com.hoaxify.hoaxify.configuration.AppConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@EnableScheduling
public class FileService {

    AppConfiguration appConfiguration;

    Tika tika;

    FileAttachmentRepository fileAttachmentRepository;

    public FileService(AppConfiguration appConfiguration,
                       FileAttachmentRepository fileAttachmentRepository) {
        this.appConfiguration = appConfiguration;
        this.fileAttachmentRepository = fileAttachmentRepository;
        this.tika = new Tika();
    }

    public String saveProfileImage(String base64Image) throws IOException {
        String imageName = getRandomName();

        byte[] decodeBytes = Base64.getDecoder().decode(base64Image);
        File target = new File(appConfiguration.getFullProfileImagesPath() + "/" + imageName);
        FileUtils.writeByteArrayToFile(target, decodeBytes);
        return imageName;
    }

    public String detectType(byte[] fileArr) {
        Tika tika = new Tika();
        return tika.detect(fileArr);
    }

    public void deleteProfileImage(String image) {
        try {
            Files.deleteIfExists(Paths.get(appConfiguration.getFullProfileImagesPath() + "/" + image));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileAttachment saveAttachment(MultipartFile file) {
        String randomName = getRandomName();
        byte[] fileAsByte = new byte[0];

        File target = new File(appConfiguration.getFullAttachmentsPath() + "/" + randomName);
        try {
            fileAsByte = file.getBytes();
            FileUtils.writeByteArrayToFile(target, fileAsByte);
        } catch (IOException e) {

        }

        return fileAttachmentRepository.save(FileAttachment.builder()
                .date(new Date())
                .name(randomName)
                .fileType(detectType(fileAsByte))
                .build());
    }

    // In milliseconds, cleans storage every hour
    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void cleanupStorage() {
        Date oneHourAgo = new Date(System.currentTimeMillis() - (60 * 60 * 1000));

        List<FileAttachment> oldFilesArray = fileAttachmentRepository.findByDateBeforeAndHoaxIsNull(oneHourAgo);

        oldFilesArray.forEach(fileAttachment -> {
            deleteAttachmentImage(fileAttachment.getName());
            fileAttachmentRepository.deleteById(fileAttachment.getId());
        });
    }

    private String getRandomName() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private void deleteAttachmentImage(String image) {
        try {
            Files.deleteIfExists(Paths.get(appConfiguration.getFullAttachmentsPath() + "/" + image));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
