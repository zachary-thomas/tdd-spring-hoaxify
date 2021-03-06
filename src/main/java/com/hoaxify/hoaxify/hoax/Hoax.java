package com.hoaxify.hoaxify.hoax;

import com.hoaxify.hoaxify.file.FileAttachment;
import com.hoaxify.hoaxify.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Hoax {

    @Id
    @GeneratedValue
    private long id;

    @NotNull
    @Size(min = 10, max = 5000)
    @Column(length = 5000)
    private String content;

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @ManyToOne
    private User user;

    // When Hoax is deleted, attachment will also be removed
    @OneToOne(mappedBy = "hoax", orphanRemoval = true)
    private FileAttachment attachment;
}
