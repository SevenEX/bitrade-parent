package cn.ztuo.bitrade.controller.cms;

import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.constant.AdminModule;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.entity.Poster;
import cn.ztuo.bitrade.entity.QPoster;
import cn.ztuo.bitrade.service.PosterService;
import cn.ztuo.bitrade.util.BindingResultUtil;
import cn.ztuo.bitrade.util.DateUtil;
import cn.ztuo.bitrade.util.MessageResult;
import cn.ztuo.bitrade.util.PredicateUtils;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.springframework.util.Assert.notNull;

/**
 * @author Paradise
 */
@RestController
@RequestMapping("/invite/poster")
@Api(tags = "海报配置管理")
public class PosterController extends BaseAdminController {

    private final PosterService posterService;

    public PosterController(PosterService posterService) {
        this.posterService = posterService;
    }

    @RequiresPermissions("invite:poster:page-query")
    @PostMapping("/create")
    @AccessLog(module = AdminModule.POSTER, operation = "创建海报")
    @ApiOperation(value = "创建海报")
    public MessageResult create(@Valid Poster poster, BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        poster.setCreateTime(DateUtil.getCurrentDate());
        poster = posterService.save(poster);
        return success(poster);
    }

    @RequiresPermissions("invite:poster:page-query")
    @PostMapping("/all")
    @ApiOperation(value = "查找所有海报")
    public MessageResult all() {
        List<Poster> posters = posterService.findAll();
        if (posters != null && posters.size() > 0) {
            return success(posters);
        }
        return error("data null");
    }

    @RequiresPermissions("invite:poster:page-query")
    @PostMapping("/detail")
    @ApiOperation(value = "海报详情")
    public MessageResult detail(@RequestParam(value = "id") Long id) {
        Poster poster = posterService.findOne(id);
        notNull(poster, "validate id!");
        return success(poster);
    }

    @RequiresPermissions("invite:poster:page-query")
    @PostMapping("/update")
    @AccessLog(module = AdminModule.HELP, operation = "更新海报")
    @ApiOperation(value = "更新海报")
    public MessageResult update(@Valid Poster poster, BindingResult bindingResult) {
        notNull(poster.getId(), "validate id!");
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        Poster one = posterService.findOne(poster.getId());
        notNull(one, "validate id!");
        poster.setCreateTime(one.getCreateTime());
        posterService.save(poster);
        return success();
    }

    @RequiresPermissions("invite:poster:page-query")
    @PostMapping("/deletes")
    @AccessLog(module = AdminModule.HELP, operation = "删除海报")
    @ApiOperation(value = "删除海报")
    public MessageResult deleteOne(@RequestParam("ids") Long[] ids) {
        posterService.deleteBatch(ids);
        return success();
    }

    @RequiresPermissions("invite:poster:page-query")
    @PostMapping("/page-query")
    @ApiOperation(value = "分页查询海报")
    public MessageResult pageQuery(String name,Integer pageNo, Integer pageSize) {
        PageModel pageModel = new PageModel();
        List<String> sorts = Collections.singletonList("createTime");
        List<Sort.Direction> directions = new ArrayList<>();
        directions.add(Sort.Direction.DESC);
        pageModel.setDirection(directions);
        pageModel.setProperty(sorts);
        pageModel.setPageNo(pageNo);
        pageModel.setPageSize(pageSize);
        List<BooleanExpression> predicates = new ArrayList<>();
        predicates.add(QPoster.poster.id.isNotNull());
        if (!StringUtils.isEmpty(name)) {
            predicates.add(QPoster.poster.name.like("%"+ name+"%"));
        }
        Page<Poster> all = posterService.findAll(PredicateUtils.getPredicate(predicates), pageModel.getPageable());
        return success(all);
    }
}
