package com.hoaxify.hoaxify.hoax;

import com.hoaxify.hoaxify.user.User;
import com.hoaxify.hoaxify.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.List;

@Service
public class HoaxService {

    HoaxRepository hoaxRepository;

    UserService userService;

    public HoaxService(HoaxRepository hoaxRepository, UserService userService) {
        this.hoaxRepository = hoaxRepository;
        this.userService = userService;
    }

    public Hoax save(Hoax hoax, User user){
        hoax.setTimestamp(new Date());
        hoax.setUser(user);
        return hoaxRepository.save(hoax);
    }

    public Page<Hoax> getAllHoaxes(Pageable pageable) {
        return hoaxRepository.findAll(pageable);
    }

    public Page<Hoax> getHoaxesOfUser(String username, Pageable pageable) {
        User inDb = userService.getByUsername(username);
        return hoaxRepository.findByUser(inDb, pageable);
    }

    public Page<Hoax> getOldHoaxes(long id, Pageable pageable) {
        return hoaxRepository.findByIdLessThan(id, pageable);
    }

    public Page<Hoax> getHoaxesRelativeForUser(String username, long id, Pageable pageable) {
        User inDb = userService.getByUsername(username);
        return hoaxRepository.findByIdLessThanAndUser(id, inDb, pageable);
    }

    public List<Hoax> getNewHoaxes(long id, Pageable pageable) {
        return hoaxRepository.findByIdGreaterThan(id, pageable.getSort());
    }
}
