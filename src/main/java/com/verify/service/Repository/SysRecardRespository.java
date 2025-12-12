package com.verify.service.Repository;

import com.verify.service.Entity.SysRecard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SysRecardRespository extends JpaRepository<SysRecard, Integer> {
   Page<SysRecard> findByMark(String mark, Pageable pageable);

   SysRecard findByRecard(String recard);
}
