package com.verify.service.Repository;

import com.verify.service.Entity.UserAppsCheckSign;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAppsCheckSignRepository extends JpaRepository<UserAppsCheckSign, Integer> {
   List<UserAppsCheckSign> findBySoftappkey(String appkey);
}
