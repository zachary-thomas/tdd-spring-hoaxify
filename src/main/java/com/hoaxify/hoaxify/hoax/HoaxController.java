package com.hoaxify.hoaxify.hoax;

import com.hoaxify.hoaxify.shared.CurrentUser;
import com.hoaxify.hoaxify.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/1.0")
public class HoaxController {

    @Autowired
    HoaxService hoaxService;

    @PostMapping("/hoaxes")
    void createHoax(@Valid @RequestBody Hoax hoax, @CurrentUser User user) {
        hoaxService.save(hoax, user);
    }

    @GetMapping("/hoaxes")
    Page<?> getHoaxes(Pageable pageable) {
        return hoaxService.getAllHoaxes(pageable);
    }

}
