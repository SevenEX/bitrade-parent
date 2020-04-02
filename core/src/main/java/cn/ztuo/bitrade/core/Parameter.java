package cn.ztuo.bitrade.core;


/**
 * @description: Parameter
 * @author: MrGao
 * @create: 2019/07/04 14:41
 */
public class Parameter {
    public int type;
    public ParameterDirection direction;
    public Object value;

    public Parameter(int type, ParameterDirection direction, Object value) {
        this.type = type;
        this.direction = direction;
        this.value = value;
    }
}
