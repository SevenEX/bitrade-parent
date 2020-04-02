package cn.ztuo.bitrade.model.screen;

import cn.ztuo.bitrade.constant.WithdrawStatus;
import lombok.Data;

@Data
public class LegalWalletWithdrawScreen {
    WithdrawStatus status;
    String username;
    String coinName;

}
