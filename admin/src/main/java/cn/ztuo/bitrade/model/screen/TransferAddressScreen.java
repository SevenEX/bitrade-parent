package cn.ztuo.bitrade.model.screen;

import cn.ztuo.bitrade.constant.CommonStatus;
import lombok.Data;

@Data
public class TransferAddressScreen {
    private CommonStatus start ;
    private String address;
    private String unit;
}
