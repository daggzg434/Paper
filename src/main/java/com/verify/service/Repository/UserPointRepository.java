package com.verify.service.Repository;

import com.verify.service.Entity.UserPoint;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPointRepository extends JpaRepository<UserPoint, Integer> {
   List<UserPoint> findAllByUseridAndSoftappkey(int user_id, String appkey);

   UserPoint findByCard(String code);

   UserPoint findBySoftappkeyAndCard(String appkey, String code);

   Page<UserPoint> findBySoftappkey(String appkey, Pageable pageable);

   Page<UserPoint> findBySoftappkeyAndMark(String appkey, String mark, Pageable pageable);

   Page<UserPoint> findBySoftappkeyAndFrozen(String appkey, int frozen, Pageable pageable);

   Page<UserPoint> findBySoftappkeyAndMacIsNull(String appkey, Pageable pageable);

   Page<UserPoint> findBySoftappkeyAndMacIsNotNull(String appkey, Pageable pageable);

   Page<UserPoint> findBySoftappkeyAndPoint(String appkey, int point, Pageable pageable);
}
