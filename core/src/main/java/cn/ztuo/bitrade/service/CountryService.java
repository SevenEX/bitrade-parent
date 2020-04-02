package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.dao.CountryDao;
import cn.ztuo.bitrade.entity.Country;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Seven
 * @date 2019年02月10日
 */
@Service
public class CountryService {
    @Autowired
    private CountryDao countryDao;

    public List<Country> getAllCountry(){
        return countryDao.findAllOrderBySort();
    }

    public Country findOne(String zhName){
        return countryDao.findByZhName(zhName);
    }

    public List<Country> findByLegalCurrency(String legalCurrency){
        return countryDao.findByLocalCurrency(legalCurrency);
    }

}
