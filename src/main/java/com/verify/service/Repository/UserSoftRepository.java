package com.verify.service.Repository;

import com.verify.service.Entity.UserSoft;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSoftRepository extends JpaRepository<UserSoft, Integer> {
   List<UserSoft> findAllByUserid(int user_id);

   UserSoft findBySoftpackagenameAndUserid(String name, int user_id);

   UserSoft findByAppkey(String appkey);
}
