package cn.ztuo.bitrade.dao;

import cn.ztuo.bitrade.constant.WorkOrderType;
import cn.ztuo.bitrade.dao.base.BaseDao;
import cn.ztuo.bitrade.entity.WorkOrder;

import java.util.List;

public interface WorkOrderDao extends BaseDao<WorkOrder> {
    List<WorkOrder> findAllByType(WorkOrderType workOrderType);
}
