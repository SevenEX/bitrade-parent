package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.entity.ExchangeTrade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExchangeTradeService {
    @Autowired
    private MongoTemplate mongoTemplate;

    public List<ExchangeTrade> findLatest(String symbol,int size){
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC,"time"));
        PageRequest page = PageRequest.of(0,size);
        query.with(page);
        return mongoTemplate.find(query,ExchangeTrade.class,"exchange_trade_"+symbol);
    }
}
