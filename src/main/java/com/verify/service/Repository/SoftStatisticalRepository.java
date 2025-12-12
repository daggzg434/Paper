package com.verify.service.Repository;

import com.verify.service.Entity.SoftStatistical;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SoftStatisticalRepository extends JpaRepository<SoftStatistical, Integer> {
   SoftStatistical findByAppkeyAndIp(String appkey, String ip);

   long countByAppkey(String appkey);

   long countByAppkeyAndTimeBetween(String appkey, Date startTime, Date endTime);

   List<SoftStatistical> findByAppkeyAndTimeBetween(String appkey, Date startTime, Date endTime);

   List<SoftStatistical> findByAppkey(String appkey);
}
