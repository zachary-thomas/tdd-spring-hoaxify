package com.hoaxify.hoaxify.hoax;

import com.hoaxify.hoaxify.file.FileAttachment;
import com.hoaxify.hoaxify.file.FileAttachmentRepository;
import com.hoaxify.hoaxify.file.FileService;
import com.hoaxify.hoaxify.user.User;
import com.hoaxify.hoaxify.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class HoaxService {

    HoaxRepository hoaxRepository;

    UserService userService;

    FileAttachmentRepository fileAttachmentRepository;

    FileService fileService;

    public HoaxService(HoaxRepository hoaxRepository,
                       UserService userService,
                       FileAttachmentRepository fileAttachmentRepository,
                       FileService fileService) {
        this.hoaxRepository = hoaxRepository;
        this.userService = userService;
        this.fileAttachmentRepository = fileAttachmentRepository;
        this.fileService = fileService;
    }

    public Hoax save(Hoax hoax, User user) {
        hoax.setTimestamp(new Date());
        hoax.setUser(user);

        if (hoax.getAttachment() != null) {
            FileAttachment inDB = fileAttachmentRepository.findById(hoax.getAttachment().getId()).get();
            inDB.setHoax(hoax);
            hoax.setAttachment(inDB);
        }

        return hoaxRepository.save(hoax);
    }

    public Page<Hoax> getAllHoaxes(Pageable pageable) {
        return hoaxRepository.findAll(pageable);
    }

    public Page<Hoax> getHoaxesOfUser(String username, Pageable pageable) {
        User inDb = userService.getByUsername(username);
        return hoaxRepository.findByUser(inDb, pageable);
    }

    public Page<Hoax> getOldHoaxes(long id, String username, Pageable pageable) {
        Specification<Hoax> specification = Specification.where(idLessThan(id));
        if (username == null) {
            return hoaxRepository.findAll(specification, pageable);
        }
        return hoaxRepository.findAll(specification.and(userIs(userService.getByUsername(username))), pageable);
    }

    public List<Hoax> getNewHoaxes(long id, String username, Pageable pageable) {
        // Leaving this method as is for example without specification
        if (username == null) {
            return hoaxRepository.findByIdGreaterThan(id, pageable.getSort());
        }
        return hoaxRepository.findByIdGreaterThanAndUser(id,
                userService.getByUsername(username),
                pageable.getSort());
    }

    public long getNewHoaxesCount(long id, String username) {
        Specification<Hoax> specification = Specification.where(idGreaterThan(id));
        if (username != null) {
            specification = specification.and(userIs(userService.getByUsername(username)));
        }
        return hoaxRepository.count(specification);
    }

    private Specification<Hoax> userIs(User user) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("user"), user);
    }

    private Specification<Hoax> idLessThan(long id) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.lessThan(root.get("id"), id);
    }

    private Specification<Hoax> idGreaterThan(long id) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get("id"), id);
    }

    public void deleteHoax(long id) {
        Hoax hoax = hoaxRepository.getById(id);
        if (hoax.getAttachment() != null) {
            fileService.deleteAttachmentImage(hoax.getAttachment().getName());
        }
        hoaxRepository.deleteById(id);
    }
}
