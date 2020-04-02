package cn.ztuo.bitrade.model.create;

import cn.ztuo.bitrade.ability.CreateAbility;
import cn.ztuo.bitrade.entity.DataDictionary;
import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * @author MrGao
 * @Title: ${file_name}
 * @Description:
 * @date 2018/4/1214:24
 */
@Data
public class DataDictionaryCreate implements CreateAbility<DataDictionary> {
    @NotBlank
    private String bond;
    @NotBlank
    private String value;
    private String comment;

    @Override
    public DataDictionary transformation() {
        return new DataDictionary(bond, value, comment);
    }
}
