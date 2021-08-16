package com.hoaxify.hoaxify.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileAttachmentVM {

    private String name;

    private String fileType;

    public FileAttachmentVM(FileAttachment fileAttachment) {
        this.name = fileAttachment.getName();
        this.fileType = fileAttachment.getFileType();
    }
}
