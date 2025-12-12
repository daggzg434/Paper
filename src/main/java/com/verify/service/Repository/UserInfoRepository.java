package com.verify.service.Repository;

import com.verify.service.Entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Integer> {
   UserInfo findByUsername(String name);

   UserInfo findByEmail(String email);

   UserInfo findByToken(String token);

   UserInfo findById(int id);

   UserInfo findByInvitation(String code);
}
