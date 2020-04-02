package cn.ztuo.bitrade.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import cn.ztuo.bitrade.core.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 认证类型
 */
@AllArgsConstructor
@Getter
public enum CredentialsType implements BaseEnum {
    CARDED("身份证"),
    PASSPORT("护照"),
    DRIVING_LICENSE("驾照"),
    OTHER("其他");

    @Setter
    private String cnName;

    @Override
    @JsonValue
    public int getOrdinal(){
        return this.ordinal();
    }

    public CredentialsType[] getAll(){
        return CredentialsType.values();
    }

    public static CredentialsType getByValue(int value){
        for(CredentialsType credentialsType:CredentialsType.values()){
            if(credentialsType.getOrdinal()==value){
                return credentialsType;
            }
        }
        return null;
    }
}
