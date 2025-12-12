package com.verify.service.Repository;

import com.verify.service.Entity.SysUpdate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SysUpdateRepository extends JpaRepository<SysUpdate, Integer> {
   SysUpdate findByVersion(int ver);

   Page<SysUpdate> findByVersionIsGreaterThan(int ver, Pageable pageable);
}
