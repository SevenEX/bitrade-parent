package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.entity.MemberLog;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface MemberLogDao extends MongoRepository<MemberLog,Long> {
}
