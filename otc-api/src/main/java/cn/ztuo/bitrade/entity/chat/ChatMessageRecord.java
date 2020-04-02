package cn.ztuo.bitrade.entity.chat;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * mogondb保存聊天消息的格式规范
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class ChatMessageRecord extends BaseMessage{

    private String content ;

    private long sendTime ;

    private String sendTimeStr ;


}
