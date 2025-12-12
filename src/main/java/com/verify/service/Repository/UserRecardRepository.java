package com.verify.service.Repository;

import com.verify.service.Entity.UserRecard;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRecardRepository extends JpaRepository<UserRecard, Integer> {
   List<UserRecard> findAllByUseridAndSoftappkey(int user_id, String appkey);

   UserRecard findByRecard(String code);

   UserRecard findBySoftappkeyAndRecard(String appkey, String code);

   Page<UserRecard> findBySoftappkey(String appkey, Pageable pageable);

   Page<UserRecard> findBySoftappkeyAndMark(String appkey, String mark, Pageable pageable);

   Page<UserRecard> findBySoftappkeyAndFrozen(String appkey, int frozen, Pageable pageable);
}
