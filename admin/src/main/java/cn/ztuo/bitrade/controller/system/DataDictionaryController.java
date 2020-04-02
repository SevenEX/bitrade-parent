package cn.ztuo.bitrade.controller.system;

import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.entity.DataDictionary;
import cn.ztuo.bitrade.model.create.DataDictionaryCreate;
import cn.ztuo.bitrade.model.update.DataDictionaryUpdate;
import cn.ztuo.bitrade.service.DataDictionaryService;
import cn.ztuo.bitrade.util.BindingResultUtil;
import cn.ztuo.bitrade.util.MessageResult;
import com.querydsl.core.BooleanBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author MrGao
 * @Title: ${file_name}
 * @Description:
 * @date 2018/4/1214:21
 */
@RestController
@RequestMapping("system/data-dictionary")
@Api(tags = "数据字典管理")
public class DataDictionaryController extends BaseAdminController {
    @Autowired
    private DataDictionaryService service;

    @PostMapping
    public MessageResult post(@Valid DataDictionaryCreate model, BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        DataDictionary data = service.findByBond(model.getBond());
        if (data != null) {
            return error("bond already existed!");
        }
        service.save(model);
        return success();
    }

    @GetMapping
    public MessageResult page(PageModel pageModel) {
        Page<DataDictionary> all = service.findAll(new BooleanBuilder(), pageModel);
        return success(all);
    }

    @PutMapping("{bond}")
    @ApiOperation(value = "更新")
    public MessageResult put(@PathVariable("bond") String bond, DataDictionaryUpdate model) {
        DataDictionary dataDictionary = service.findByBond(bond);
        Assert.notNull(dataDictionary, "validate bond");
        service.update(model, dataDictionary);
        return success();
    }

    @GetMapping("{bond}")
    @ApiOperation(value = "查询")
    public MessageResult get(@PathVariable("bond") String bond) {
        return success(service.findByBond(bond).getValue());
    }

}
