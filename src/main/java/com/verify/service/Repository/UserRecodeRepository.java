package com.verify.service.Repository;

import com.verify.service.Entity.UserRecode;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRecodeRepository extends JpaRepository<UserRecode, Integer> {
   List<UserRecode> findAllByUseridAndSoftappkey(int user_id, String appkey);

   UserRecode findByCard(String code);

   List<UserRecode> findAllByMac(String mac);

   List<UserRecode> findAllByMacAndSoftappkey(String mac, String appkey);

   UserRecode findBySoftappkeyAndCard(String appkey, String code);

   Page<UserRecode> findBySoftappkey(String appkey, Pageable pageable);

   Page<UserRecode> findBySoftappkeyAndMark(String appkey, String mark, Pageable pageable);

   Page<UserRecode> findBySoftappkeyAndFrozen(String appkey, int frozen, Pageable pageable);

   Page<UserRecode> findBySoftappkeyAndAllminutes(String appkey, int minutes, Pageable pageable);

   Page<UserRecode> findBySoftappkeyAndUsecount(String appkey, int usecount, Pageable pageable);

   Page<UserRecode> findBySoftappkeyAndMacIsNull(String appkey, Pageable pageable);

   Page<UserRecode> findBySoftappkeyAndMacIsNotNull(String appkey, Pageable pageable);
}
