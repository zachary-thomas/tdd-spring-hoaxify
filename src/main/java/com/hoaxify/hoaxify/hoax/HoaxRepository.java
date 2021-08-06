package com.hoaxify.hoaxify.hoax;

import com.hoaxify.hoaxify.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

// JpaSpecificationExecutor provides use in HoaxService to not need as many methods here
public interface HoaxRepository extends JpaRepository<Hoax, Long>, JpaSpecificationExecutor<Hoax> {

    Page<Hoax> findByUser(User user, Pageable pageable);

    Page<Hoax> findByUserUsername(String username, Pageable pageable);

    Page<Hoax> findByIdLessThan(long id, Pageable pageable);

    Page<Hoax> findByIdLessThanAndUser(long id, User user, Pageable pageable);

    List<Hoax> findByIdGreaterThan(long id, Sort sort);

    List<Hoax> findByIdGreaterThanAndUser(long id, User user, Sort sort);

    long countByUserUsername(String username);

    long countByIdGreaterThan(long id);

    long countByIdGreaterThanAndUser(long id, User user);

}
