package com.verify.service.Repository;

import com.verify.service.Entity.SoftUser;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SoftUserRepository extends JpaRepository<SoftUser, Integer> {
   SoftUser findByUsernameAndSoftappkey(String username, String appkey);

   List<SoftUser> findBySoftappkey(String appkey);

   Page<SoftUser> findBySoftappkey(String appkey, Pageable pageable);

   List<SoftUser> findBySoftappkeyAndMac(String appkey, String mac);

   List<SoftUser> findBySoftappkeyAndEmail(String appkey, String email);

   Page<SoftUser> findBySoftappkeyAndExpiretimeBefore(String appkey, Date time, Pageable pageable);

   Page<SoftUser> findBySoftappkeyAndExpiretimeAfter(String appkey, Date time, Pageable pageable);

   Page<SoftUser> findBySoftappkeyAndFrozen(String appkey, int frozen, Pageable pageable);
}
