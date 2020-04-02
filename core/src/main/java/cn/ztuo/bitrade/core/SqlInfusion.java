package cn.ztuo.bitrade.core;

import org.apache.commons.lang3.StringUtils;

/**
 * @description: SqlInfusion
 * @author: MrGao
 * @create: 2019/07/04 14:50
 */
public class SqlInfusion {
    public SqlInfusion() {
    }

    public static String FilterSqlInfusion(String input) {
        if (input != null && input.trim() != "") {
            return !StringUtils.isNumeric(input) ? input.replaceAll("\\b(drop|exec|execute|create|truncate|delete|insert|update)\\b", "`$1`") : input;
        } else {
            return "";
        }
    }
}
