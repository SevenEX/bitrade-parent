package cn.ztuo.bitrade.model.screen;

import cn.ztuo.bitrade.constant.CommonStatus;
import cn.ztuo.bitrade.constant.SysAdvertiseLocation;
import lombok.Data;

@Data
public class SysAdvertiseScreen {
    private String serialNumber;
    private String name;
    private SysAdvertiseLocation sysAdvertiseLocation;
    private CommonStatus status;
}
