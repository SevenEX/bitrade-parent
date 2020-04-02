package cn.ztuo.bitrade.service;

import cn.ztuo.bitrade.constant.BooleanEnum;
import cn.ztuo.bitrade.constant.CommonStatus;
import cn.ztuo.bitrade.core.Model;
import cn.ztuo.bitrade.dao.CoinDao;
import cn.ztuo.bitrade.dao.MemberAddressDao;
import cn.ztuo.bitrade.entity.Coin;
import cn.ztuo.bitrade.entity.MemberAddress;
import cn.ztuo.bitrade.pagination.Criteria;
import cn.ztuo.bitrade.pagination.Restrictions;
import cn.ztuo.bitrade.service.Base.BaseService;
import cn.ztuo.bitrade.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Seven
 * @date 2019年01月26日
 */
@Service
public class MemberAddressService extends BaseService {
    @Autowired
    private MemberAddressDao memberAddressDao;
    @Autowired
    private CoinDao coinDao;
    @Autowired
    private LocaleMessageSourceService messageSource;

    public MessageResult addMemberAddress(Long memberId, String address, String unit, String remark) {
        Coin coin = coinDao.findByUnit(unit);
        if (coin == null || coin.getCanWithdraw().equals(BooleanEnum.IS_FALSE)) {
            return MessageResult.error(600, messageSource.getMessage("CURRENCY.NOT.SUPORT"));
        }
        MemberAddress memberAddress = new MemberAddress();
        memberAddress.setAddress(address);
        memberAddress.setCoin(coin);
        memberAddress.setMemberId(memberId);
        memberAddress.setRemark(remark);
        MemberAddress memberAddress1=memberAddressDao.saveAndFlush(memberAddress);
        if (memberAddress1!=null){
            return MessageResult.success();
        }else {
            return MessageResult.error(messageSource.getMessage("FAIL"));
        }
    }

    public MessageResult deleteMemberAddress(Long memberId,Long addressId){
        int is=memberAddressDao.deleteMemberAddress(new Date(), addressId, memberId);
        if (is>0){
            return MessageResult.success();
        }else {
            return MessageResult.error(messageSource.getMessage("FAIL"));
        }
    }

    public Page<MemberAddress> pageQuery(int pageNo, Integer pageSize, long id,String unit) {
        Sort orders = Criteria.sortStatic("id.desc");
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, orders);
        Criteria<MemberAddress> specification = new Criteria<>();
        specification.add(Restrictions.eq("memberId",id,false));
        specification.add(Restrictions.eq("status", CommonStatus.NORMAL, false));
        specification.add(Restrictions.eq("coin.unit",unit,false));
        return memberAddressDao.findAll(specification, pageRequest);
    }

    public List<Map<String,String>> queryAddress(long userId,String coinId)  {
        try {
            return new Model("member_address")
                    .field(" remark,address")
                    .where("member_id=? and coin_id=? and status=?", userId, coinId, CommonStatus.NORMAL.ordinal())
                    .select();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public List<MemberAddress> findByMemberIdAndAddress(long userId,String address){
        return memberAddressDao.findAllByMemberIdAndAddressAndStatus(userId,address,CommonStatus.NORMAL);
    }

    public List<MemberAddress> findByMemberIdAndCoinAndAddress(long userId,Coin coin ,String address,CommonStatus status){
        return  memberAddressDao.findByMemberIdAndCoinAndAddressAndStatus(userId,coin,address,status);
    }
}
