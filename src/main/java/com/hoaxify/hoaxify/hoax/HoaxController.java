package com.hoaxify.hoaxify.hoax;

import com.hoaxify.hoaxify.shared.CurrentUser;
import com.hoaxify.hoaxify.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/1.0")
public class HoaxController {

    @Autowired
    HoaxService hoaxService;

    @PostMapping("/hoaxes")
    HoaxVM createHoax(@Valid @RequestBody Hoax hoax, @CurrentUser User user) {
        return new HoaxVM(hoaxService.save(hoax, user));
    }

    @GetMapping("/hoaxes")
    Page<HoaxVM> getHoaxes(Pageable pageable) {
        return hoaxService.getAllHoaxes(pageable).map(HoaxVM::new);
    }

    @GetMapping("/users/{username}/hoaxes")
    Page<HoaxVM> getHoaxesOfUser(@PathVariable String username, Pageable pageable) {
        return hoaxService.getHoaxesOfUser(username, pageable).map(HoaxVM::new);
    }

    @GetMapping("/hoaxes/{id:[0-9]+}")
    ResponseEntity<?> getHoaxesRelative(@PathVariable long id,
                                        @RequestParam(name = "direction", defaultValue = "after") String direction,
                                        Pageable pageable) {
        if (!direction.equalsIgnoreCase("after")) {
            return ResponseEntity.ok(hoaxService.getOldHoaxes(id, pageable).map(HoaxVM::new));
        }
        return ResponseEntity.ok(hoaxService.getNewHoaxes(id, pageable)
                .stream()
                .map(HoaxVM::new)
                .collect(Collectors.toList()));
    }

    @GetMapping("/users/{username}/hoaxes/{id:[0-9]+}")
    Page<?> getHoaxesRelativeForUser(@PathVariable String username, @PathVariable long id, Pageable pageable) {
        return hoaxService.getHoaxesRelativeForUser(username, id, pageable).map(HoaxVM::new);
    }

}
