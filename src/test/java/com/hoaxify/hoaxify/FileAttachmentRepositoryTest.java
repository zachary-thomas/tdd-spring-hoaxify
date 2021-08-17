package com.hoaxify.hoaxify;

import com.hoaxify.hoaxify.file.FileAttachment;
import com.hoaxify.hoaxify.file.FileAttachmentRepository;
import com.hoaxify.hoaxify.hoax.Hoax;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
class FileAttachmentRepositoryTest {

    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    FileAttachmentRepository fileAttachmentRepository;

    @Test
    public void findByDateBeforeAndHoaxIsNull_whenAttachmentsDateOlderThanOneHour_returnsAll() {
        testEntityManager.persist(getOneHourOldFileAttachment());
        testEntityManager.persist(getOneHourOldFileAttachment());
        testEntityManager.persist(getOneHourOldFileAttachment());

        Date oneHourAgo = new Date(System.currentTimeMillis() - (60 * 60 * 1000));

        List<FileAttachment> fileAttachmentList = fileAttachmentRepository.findByDateBeforeAndHoaxIsNull(oneHourAgo);

        assertThat(fileAttachmentList.size()).isEqualTo(3);
    }

    @Test
    public void findByDateBeforeAndHoaxIsNull_whenAttachmentsDateOlderThanOneHourButHaveHoax_returnsNone() {
        Hoax hoax1 = testEntityManager.persist(TestUtil.createValidHoax());
        Hoax hoax2 = testEntityManager.persist(TestUtil.createValidHoax());
        Hoax hoax3 = testEntityManager.persist(TestUtil.createValidHoax());

        testEntityManager.persist(getOldFileAttachmentWithHoax(hoax1));
        testEntityManager.persist(getOldFileAttachmentWithHoax(hoax2));
        testEntityManager.persist(getOldFileAttachmentWithHoax(hoax3));

        Date oneHourAgo = new Date(System.currentTimeMillis() - (60 * 60 * 1000));

        List<FileAttachment> fileAttachmentList = fileAttachmentRepository.findByDateBeforeAndHoaxIsNull(oneHourAgo);

        assertThat(fileAttachmentList.size()).isEqualTo(0);
    }

    @Test
    public void findByDateBeforeAndHoaxIsNull_whenAttachmentsDateWithinOneHour_returnsNone() {
        testEntityManager.persist(getFileAttachmentWithinOneHour());
        testEntityManager.persist(getFileAttachmentWithinOneHour());
        testEntityManager.persist(getFileAttachmentWithinOneHour());

        Date oneHourAgo = new Date(System.currentTimeMillis() - (60 * 60 * 1000));

        List<FileAttachment> fileAttachmentList = fileAttachmentRepository.findByDateBeforeAndHoaxIsNull(oneHourAgo);

        assertThat(fileAttachmentList.size()).isEqualTo(0);
    }

    @Test
    public void findByDateBeforeAndHoaxIsNull_whenSomeAttachmentsOldSomeNewAndSomeWithHoax_returnsAttachmentsOlderAndNoHoaxAssigned() {
        Hoax hoax1 = testEntityManager.persist(TestUtil.createValidHoax());

        testEntityManager.persist(getOldFileAttachmentWithHoax(hoax1));
        testEntityManager.persist(getOneHourOldFileAttachment());
        testEntityManager.persist(getFileAttachmentWithinOneHour());

        Date oneHourAgo = new Date(System.currentTimeMillis() - (60 * 60 * 1000));

        List<FileAttachment> fileAttachmentList = fileAttachmentRepository.findByDateBeforeAndHoaxIsNull(oneHourAgo);

        assertThat(fileAttachmentList.size()).isEqualTo(1);
    }



    /*
    Private Helper Methods
     */

    private FileAttachment getOneHourOldFileAttachment() {
        Date date = new Date(System.currentTimeMillis() - (60 * 60 * 1000) - 1);
        return FileAttachment.builder()
                .date(date)
                .build();
    }

    private FileAttachment getFileAttachmentWithinOneHour() {
        Date date = new Date(System.currentTimeMillis() - (60 * 1000));
        return FileAttachment.builder()
                .date(date)
                .build();
    }

    private FileAttachment getOldFileAttachmentWithHoax(Hoax hoax) {
        Date date = new Date(System.currentTimeMillis() - (60 * 60 * 1000) - 1);
        return FileAttachment.builder()
                .hoax(hoax)
                .date(date)
                .build();
    }

}