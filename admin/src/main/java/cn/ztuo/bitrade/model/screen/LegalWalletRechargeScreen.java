package cn.ztuo.bitrade.model.screen;

import cn.ztuo.bitrade.constant.LegalWalletState;
import lombok.Data;

@Data
public class LegalWalletRechargeScreen {
    LegalWalletState status;
    String username;
    String coinName;

}
