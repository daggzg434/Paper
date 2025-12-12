package com.verify.service.Repository;

import com.verify.service.Entity.TrialInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrialRepository extends JpaRepository<TrialInfo, Integer> {
   TrialInfo findByMacAndAppkey(String mac, String appkey);
}
