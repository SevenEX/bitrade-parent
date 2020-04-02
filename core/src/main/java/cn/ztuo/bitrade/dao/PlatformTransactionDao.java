package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.entity.PlatformTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformTransactionDao extends JpaRepository<PlatformTransaction,Long>{
}
