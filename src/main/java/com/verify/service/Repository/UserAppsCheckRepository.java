package com.verify.service.Repository;

import com.verify.service.Entity.UserAppsCheck;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAppsCheckRepository extends JpaRepository<UserAppsCheck, Integer> {
   List<UserAppsCheck> findBySoftappkey(String appkey);
}
