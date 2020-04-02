package cn.ztuo.bitrade.model.update;

import cn.ztuo.bitrade.ability.UpdateAbility;
import cn.ztuo.bitrade.entity.DataDictionary;
import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * @author MrGao
 * @Title: ${file_name}
 * @Description:
 * @date 2018/4/1214:46
 */
@Data
public class DataDictionaryUpdate implements UpdateAbility<DataDictionary> {
    @NotBlank
    private String value;
    private String comment;

    @Override
    public DataDictionary transformation(DataDictionary dataDictionary) {
        dataDictionary.setValue(value);
        dataDictionary.setComment(comment);
        return dataDictionary;
    }
}
