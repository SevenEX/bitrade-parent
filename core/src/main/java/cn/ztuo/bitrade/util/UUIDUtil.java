package cn.ztuo.bitrade.util;

import java.math.BigInteger;
import java.util.UUID;

public class UUIDUtil {

    public static String getUUID(){
        return UUID.randomUUID().toString().replace("-","").toUpperCase() ;
    }

    public static String getIdFromMemberIdAndUnit(long memberId, String unit) {
        int offset = Math.abs(unit.hashCode()) % 20000 + 10000;
        BigInteger num = BigInteger.valueOf(memberId).multiply(BigInteger.valueOf(11)).add(BigInteger.valueOf(offset));
        return String.valueOf(num.intValue());
    }

    public static void main(String[] args) {
        System.out.println(getIdFromMemberIdAndUnit(100001, "EOS"));
    }
}
