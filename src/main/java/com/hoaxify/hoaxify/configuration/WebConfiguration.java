package com.hoaxify.hoaxify.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.validation.Valid;
import java.io.File;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    @Autowired
    AppConfiguration appConfiguration;

    @Bean
    CommandLineRunner createUploadFolder() {
        return (args) -> {
            createNonEsistingFolder(appConfiguration.getUploadPath());
            createNonEsistingFolder(appConfiguration.getFullProfileImagesPath());
            createNonEsistingFolder(appConfiguration.getFullAttachmentsPath());
        };
    }

    private void createNonEsistingFolder(String path) {
        File folder = new File(path);
        boolean uploadFolderExist = folder.exists() && folder.isDirectory();

        if (!uploadFolderExist) {
            folder.mkdir();
        }
    }


}
