package com.hoaxify.hoaxify;

import com.hoaxify.hoaxify.configuration.AppConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class StaticResourceTest {

    @Autowired
    AppConfiguration appConfiguration;

    @Test
    public void checkStaticFolder_WhenAppIsInitialized_uploadFolderMustExist(){
        File uploadFolder = new File(appConfiguration.getUploadPath());
        boolean uploadFolderExists = uploadFolder.exists() && uploadFolder.isDirectory();

        assertThat(uploadFolderExists).isTrue();
    }

    @Test
    public void checkStaticFolder_whenAppIsInitialized_profileImageSubFOlderMustExist(){
        String profileImageFolderPath = appConfiguration.getFullProfileImagesPath();
        File profileImageFolder = new File(appConfiguration.getUploadPath());
        boolean profileImageFolderExists = profileImageFolder.exists() && profileImageFolder.isDirectory();

        assertThat(profileImageFolderExists).isTrue();
    }

    @Test
    public void checkStaticFolder_whenAppIsInitialized_attachmentsSubFolderMustExist(){
        String attachmentsFolderPath = appConfiguration.getFullAttachmentsPath();
        File attachmentsFolder = new File(appConfiguration.getUploadPath());
        boolean attachmentsFolderExists = attachmentsFolder.exists() && attachmentsFolder.isDirectory();

        assertThat(attachmentsFolderExists).isTrue();
    }

}
