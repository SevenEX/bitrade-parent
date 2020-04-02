
package cn.ztuo.bitrade.controller.system;

import cn.ztuo.bitrade.annotation.AccessLog;
import cn.ztuo.bitrade.annotation.MultiDataSource;
import cn.ztuo.bitrade.constant.AdminModule;
import cn.ztuo.bitrade.constant.PageModel;
import cn.ztuo.bitrade.constant.SmsCodePrefixEnum;
import cn.ztuo.bitrade.constant.SysConstant;
import cn.ztuo.bitrade.controller.common.BaseAdminController;
import cn.ztuo.bitrade.core.Convert;
import cn.ztuo.bitrade.core.Encrypt;
import cn.ztuo.bitrade.core.Menu;
import cn.ztuo.bitrade.entity.Admin;
import cn.ztuo.bitrade.entity.Country;
import cn.ztuo.bitrade.entity.QAdmin;
import cn.ztuo.bitrade.entity.SysRole;
import cn.ztuo.bitrade.service.AdminService;
import cn.ztuo.bitrade.service.CountryService;
import cn.ztuo.bitrade.service.SysPermissionService;
import cn.ztuo.bitrade.service.SysRoleService;
import cn.ztuo.bitrade.util.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Seven
 * @date 2019年12月19日
 */


@Slf4j
@Controller
@RequestMapping("/system/employee")
@Api(tags = "管理员管理")
public class EmployeeController extends BaseAdminController {

    @Value("${bdtop.system.md5.key}")
    private String md5Key;

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private AdminService adminService;

    @Resource
    private SysPermissionService sysPermissionService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CountryService countryService;


    /**
     * 提交登录信息
     *
     * @param request
     * @return
     */


    @ResponseBody
    //@AccessLog(module = AdminModule.EMPLOYEE, operation = "提交登录信息Admin")
    @ApiOperation(value = "手机验证码登录")
    @RequestMapping(value = "sign/in", method = RequestMethod.POST)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名", required = true, dataType = "String"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "String"),
            @ApiImplicitParam(name = "phone", value = "手机号", required = true, dataType = "String"),
            @ApiImplicitParam(name = "code", value = "验证码", required = true, dataType = "String"),
    })
    @MultiDataSource(name = "second")
    public MessageResult doLogin(@SessionAttribute("username") String username,
                                 @SessionAttribute("password") String password,
                                 @SessionAttribute("phone") String phone, String code,
                                 //   @RequestParam(value="rememberMe",defaultValue = "true")boolean rememberMe,
                                 HttpServletRequest request) {
        //
        Assert.notNull(code, msService.getMessage("MISSING_VERIFICATION_CODE"));
        Assert.isTrue(StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password) && StringUtils.isNotEmpty(phone), "会话已过期");

        Object cacheCode = redisUtil.get(SysConstant.ADMIN_LOGIN_PHONE_PREFIX + phone);
        Assert.notNull(cacheCode, "验证码已经被清除，请重新发送");
        if (!code.equals(cacheCode.toString())) {
            return error("手机验证码错误，请重新输入");
        }
        try {
            log.info("md5Key {}", md5Key);
            UsernamePasswordToken token = new UsernamePasswordToken(username, password, true);
            token.setHost(getRemoteIp(request));
            SecurityUtils.getSubject().login(token);

            redisUtil.delete(SysConstant.ADMIN_LOGIN_PHONE_PREFIX + phone);
            Admin admin = (Admin) request.getSession().getAttribute(SysConstant.SESSION_ADMIN);

            //token.setRememberMe(true);

            //获取当前用户的菜单权限
            List<Menu> list;
            if (admin.getUsername().equalsIgnoreCase("root")) {
                list = sysRoleService.toMenus(sysPermissionService.findAll(), 0L);
            } else {
                list = sysRoleService.toMenus(sysRoleService.getPermissions(admin.getRoleId()), 0L);
            }
            Map<String, Object> map = new HashMap<>();
            map.put("permissions", list);
            map.put("admin", admin);
            return success("登录成功", map);
        } catch (AuthenticationException e) {
            e.printStackTrace();
            return error(e.getMessage());
        }
    }


    @ResponseBody
    @AccessLog(module = AdminModule.SYSTEM, operation = "登录")
    @ApiOperation(value = "登录验证")
    @RequestMapping(value = "/check", method = RequestMethod.POST)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名", required = true, dataType = "String"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "String"),
            @ApiImplicitParam(name = "code", value = "验证码", required = true, dataType = "String"),
    })
    @MultiDataSource(name = "second")
    public MessageResult valiatePhoneCode(HttpServletRequest request) {
        String username = Convert.strToStr(request(request, "username"), "");
        String password = Convert.strToStr(request(request, "password"), "");
        String captcha = Convert.strToStr(request(request, "captcha"), "");
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            return error("用户名或密码不能为空");
        }
        HttpSession session = request.getSession();
//        if (StringUtils.isBlank(captcha)) {
//            return error("验证码不能为空");
//        }
        String ADMIN_LOGIN = "ADMIN_LOGIN";
        if (!CaptchaUtil.validate(session, ADMIN_LOGIN, captcha)) {
            return error(msService.getMessage("VERIFY_CODE_INCORRECT"));
        }
        password = Encrypt.MD5(password + md5Key);
        Admin admin = adminService.login(username, password);
        if (admin == null) {
            return error("用户名或密码不存在");
        } else {
            try {
                request.getSession().setAttribute("username", username);
                request.getSession().setAttribute("password", password);
                request.getSession().setAttribute("phone", admin.getMobilePhone());

                return success("", admin.getMobilePhone());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return error("验证失败");
        }
    }


    @ResponseBody
    @AccessLog(module = AdminModule.SYSTEM, operation = "登录")
    @ApiOperation(value = "登录")
    @RequestMapping(value = "googleAuth/sign/in", method = RequestMethod.POST)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名", required = true, dataType = "String"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "String"),
            @ApiImplicitParam(name = "captcha", value = "验证码", required = true, dataType = "String"),
    })
    @MultiDataSource(name = "second")
    public MessageResult dogoogleAuthLogin(String username, String password, String captcha, HttpServletRequest request) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            return error("用户名或密码不能为空");
        }
        password = Encrypt.MD5(password + md5Key);
        Admin admin = adminService.login(username, password);
        if (admin == null) {
            return error("用户名或密码不存在");
        } else {
//            if(admin.getGoogleState()!=null&&admin.getGoogleState()==1){
//                request.getSession().setAttribute("username",username);
//                request.getSession().setAttribute("password",password);
//                return success("googleAuth");
//            }
            String ADMIN_LOGIN = "ADMIN_LOGIN";
            if (!CaptchaUtil.validate(request.getSession(), ADMIN_LOGIN, captcha)) {
              return error("验证码不正确");
            }
        }
        try {
            log.info("md5Key {}", md5Key);
            UsernamePasswordToken token = new UsernamePasswordToken(username, password, true);
            token.setHost(getRemoteIp(request));
            SecurityUtils.getSubject().login(token);
            //获取当前用户的菜单权限
            List<Menu> list;
            if (admin.getUsername().equalsIgnoreCase("root")) {
                list = sysRoleService.toMenus(sysPermissionService.findAll(), 0L);
            } else {
                list = sysRoleService.toMenus(sysRoleService.getPermissions(admin.getRoleId()), 0L);
            }
            Map<String, Object> map = new HashMap<>();
            map.put("permissions", list);
            map.put("admin", admin);
            return success("登录成功", map);
        } catch (AuthenticationException e) {
            e.printStackTrace();
            return error(e.getMessage());
        }
    }


    @ResponseBody
    @AccessLog(module = AdminModule.SYSTEM, operation = "谷歌验证码登录")
    @ApiOperation(value = "谷歌验证码登录")
    @RequestMapping(value = "googleAuth", method = RequestMethod.POST)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名", required = true, dataType = "String"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "String"),
            @ApiImplicitParam(name = "codes", value = "谷歌验证码", required = true, dataType = "String"),
    })
    @MultiDataSource(name = "second")
    public MessageResult googleAuthLogin(@SessionAttribute("username") String username,
                                         @SessionAttribute("password") String password,
                                         HttpServletRequest request, String codes) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            return error("用户名或密码不能为空");
        }
        Admin admin = adminService.login(username, password);
        if (admin == null) {
            return error("用户名或密码不存在");
        }
        try {
            long code = Long.parseLong(codes);
            long t = System.currentTimeMillis();
            GoogleAuthenticatorUtil ga = new GoogleAuthenticatorUtil();
            //  ga.setWindowSize(0); // should give 5 * 30 seconds of grace...
            boolean r = ga.check_code(admin.getGoogleKey(), code, t);
            log.info("rrrr=" + r);
            if (!r) {
                return MessageResult.error("验证失败");
            }
            log.info("md5Key {}", md5Key);
            UsernamePasswordToken token = new UsernamePasswordToken(username, password, true);
            token.setHost(getRemoteIp(request));
            SecurityUtils.getSubject().login(token);
            //获取当前用户的菜单权限
            List<Menu> list;
            if (admin.getUsername().equalsIgnoreCase("root")) {
                list = sysRoleService.toMenus(sysPermissionService.findAll(), 0L);
            } else {
                list = sysRoleService.toMenus(sysRoleService.getPermissions(admin.getRoleId()), 0L);
            }
            Map<String, Object> map = new HashMap<>();
            map.put("permissions", list);
            map.put("admin", admin);
            return success("登录成功", map);
        } catch (AuthenticationException e) {
            e.printStackTrace();
            return error(e.getMessage());
        }
    }


    /**
     * 退出登录
     *
     * @return
     */


    @ResponseBody
    @ApiOperation(value = "退出登录")
    @RequestMapping(value = "logout", method = RequestMethod.POST)
    @AccessLog(module = AdminModule.SYSTEM, operation = "退出登录")
    @MultiDataSource(name = "second")
    public MessageResult logout() {
        SecurityUtils.getSubject().logout();
        return success();
    }


    /**
     * 创建或更改后台用户
     *
     * @param admin
     * @param bindingResult
     * @return
     */
    @RequiresPermissions("system:employee:page-query")
    @ResponseBody
    @AccessLog(module = AdminModule.EMPLOYEE, operation = "创建或更改后台用户")
    @ApiOperation(value = "创建或更改后台用户")
    @RequestMapping(value = "/merge", method = RequestMethod.POST)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户编号", dataType = "Long"),
            @ApiImplicitParam(name = "username", value = "用户名", required = true, dataType = "String"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "String"),
            @ApiImplicitParam(name = "code", value = "短信验证码", required = true, dataType = "String")
    })
    @Transactional(rollbackFor = Exception.class)
    public MessageResult addAdmin(Admin admin, String code,
                                  @SessionAttribute(SysConstant.SESSION_ADMIN) @ApiIgnore Admin currentAdmin,
                                  BindingResult bindingResult) {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        String password;
        if (admin.getId() != null) {
            checkSmsCode(currentAdmin, code, SmsCodePrefixEnum.ADMIN_UPDATE_PHONE_PREFIX);
            Admin admin1 = adminService.findOne(admin.getId());
            admin.setLastLoginIp(admin1.getLastLoginIp());
            admin.setLastLoginTime(admin1.getLastLoginTime());
            //如果密码不为null更改密码
            if (StringUtils.isNotBlank(admin.getPassword())) {
                password = Encrypt.MD5(admin.getPassword() + md5Key);
            } else {
                password = admin1.getPassword();
            }
            admin.setCreateTime(admin1.getCreateTime());
        } else {
            checkSmsCode(currentAdmin, code, SmsCodePrefixEnum.ADMIN_ADD_PHONE_PREFIX);
            //这里是新增
            Admin a = adminService.findByUsername(admin.getUsername());
            if (a != null) {
                return error("用户名已存在！");
            }
            if (StringUtils.isBlank(admin.getPassword())) {
                return error("密码不能为空");
            }
            password = Encrypt.MD5(admin.getPassword() + md5Key);
            admin.setCreateTime(DateUtil.getCurrentDate());
        }

        admin.setPassword(password);
        adminService.saveAdmin(admin);
        return success("操作成功");
    }

    @ResponseBody
    @RequiresPermissions("system:employee:page-query")
    // @AccessLog(module = AdminModule.EMPLOYEE, operation = "分页查找后台用户admin")
    @ApiOperation(value = "分页查找后台用户")
    @RequestMapping(value = "page-query", method = RequestMethod.POST)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "页码", required = true, dataType = "Integer", defaultValue = "1"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", required = true, dataType = "Integer", defaultValue = "10"),
            @ApiImplicitParam(name = "username", value = "账号", required = true, dataType = "String"),
    })
    @MultiDataSource(name = "second")
    public MessageResult findAllAdminUser(
            PageModel pageModel,
            // @RequestParam(value = "searchKey", defaultValue = "") String searchKey,
            @RequestParam(value = "username", defaultValue = "") String username,
            @RequestParam(value = "mobilePhone", defaultValue = "") String mobilePhone) {
        //BooleanExpression predicate = QAdmin.admin.id.ne(Long.parseLong("1"));
        List<BooleanExpression> predicate = new ArrayList<>();
        predicate.add(QAdmin.admin.id.ne(Long.parseLong("1")));
        if (StringUtils.isNotBlank(username)) {
            predicate.add(QAdmin.admin.username.like("%" + username + "%"));
        }
        if (StringUtils.isNotBlank(mobilePhone)) {
            predicate.add(QAdmin.admin.mobilePhone.like("%" + mobilePhone + "%"));
        }
        Page<Admin> all = adminService.findAll(PredicateUtils.getPredicate(predicate), pageModel.getPageable());
        for (Admin admin : all.getContent()) {
            SysRole role = sysRoleService.findOne(admin.getRoleId());
            admin.setRoleName(role.getRole());
        }
        return success(all);
    }

    @PostMapping("update-password")
    @ResponseBody
    @AccessLog(module = AdminModule.EMPLOYEE, operation = "修改用户密码")
    @ApiOperation(value = "修改用户密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "lastPassword", value = "原密码", required = true, dataType = "String"),
            @ApiImplicitParam(name = "newPassword", value = "新密码", required = true, dataType = "String"),
    })
    public MessageResult updatePassword(Long id, String lastPassword, String newPassword) {
        Assert.notNull(id, "admin id 不能为null");
        Assert.notNull(lastPassword, "请输入原密码");
        Assert.notNull(newPassword, "请输入新密码");
        Admin admin = adminService.findOne(id);
        lastPassword = Encrypt.MD5(lastPassword + md5Key);
        Assert.isTrue(lastPassword.equalsIgnoreCase(admin.getPassword()), "密码错误");
        admin.setPassword(Encrypt.MD5(newPassword + md5Key));
        adminService.save(admin);
        return MessageResult.success(msService.getMessage("SUCCESS"));
    }

    @RequiresPermissions("system:employee:page-query")
    @PostMapping("reset-password")
    @ResponseBody
    @ApiOperation(value = "重置密码")
    @AccessLog(module = AdminModule.EMPLOYEE, operation = "重置密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户id", required = true, dataType = "Long"),
    })
    public MessageResult resetPassword(Long id) {
        Assert.notNull(id, "admin id 不能为null");
        Admin admin = adminService.findOne(id);
        admin.setPassword(Encrypt.MD5("123456" + md5Key));
        adminService.save(admin);
        return MessageResult.success("重置密码成功，默认密码123456");
    }


    /**
     * admin信息
     *
     * @param id
     * @return
     */


    @RequiresPermissions("system:employee:page-query")
    @ResponseBody
    //@AccessLog(module = AdminModule.EMPLOYEE, operation = "后台用户Admin详情")
    @ApiOperation(value = "管理员详情")
    @RequestMapping(value = "detail", method = RequestMethod.POST)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户id", required = true, dataType = "Long"),
    })
    @MultiDataSource(name = "second")
    public MessageResult adminDetail(Long id) {
        Map map = adminService.findAdminDetail(id);
        MessageResult result = success();
        result.setData(map);
        return result;
    }


    /**
     * admin信息
     *
     * @return
     */
    @RequiresPermissions("system:employee:page-query")
    @RequestMapping(value = "/deletes", method = RequestMethod.POST)
    @ResponseBody
    @AccessLog(module = AdminModule.EMPLOYEE, operation = "删除后台用户")
    @ApiOperation(value = "删除后台用户")
    public MessageResult deletes(Long[] ids, String code,
                                 @SessionAttribute(SysConstant.SESSION_ADMIN) @ApiIgnore Admin currentAdmin) {
        checkSmsCode(currentAdmin, code, SmsCodePrefixEnum.ADMIN_DEL_PHONE_PREFIX);
        adminService.deletes(ids);
        return MessageResult.success("批量删除成功");
    }

    @RequestMapping(value = "lock-screen", method = RequestMethod.POST)
    @ResponseBody
    @AccessLog(module = AdminModule.SYSTEM, operation = "锁屏密码验证")
    @ApiOperation(value = "锁屏密码验证")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名", required = true, dataType = "String"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "String"),
    })
    @MultiDataSource(name = "second")
    public MessageResult lockScreen(@SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin, String password) {
        Admin admin1 = adminService.login(admin.getUsername(), Encrypt.MD5(password + md5Key));
        if (admin1 != null) {
            return success("验证成功");
        }
        return error("密码错误");
    }

    @PostMapping("allCountry")
    @ResponseBody
    @ApiOperation(value = "获取所有的国家/地区")
    @MultiDataSource(name = "second")
    public MessageResult getAllCountry() {
        List<Country> countryList = countryService.getAllCountry();
        MessageResult result = MessageResult.success();
        result.setData(countryList);
        return result;
    }


    @PostMapping("change-password")
    @ResponseBody
    @ApiOperation(value = "修改自己的密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "lastPassword", value = "原密码", required = true, dataType = "String"),
            @ApiImplicitParam(name = "newPassword", value = "新密码", required = true, dataType = "String"),
    })
    public MessageResult changePassword(@SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin, String lastPassword, String newPassword) {
        Assert.notNull(admin.getId(), "admin id 不能为null");
        Assert.notNull(lastPassword, "请输入原密码");
        Assert.notNull(newPassword, "请输入新密码");
        lastPassword = Encrypt.MD5(lastPassword + md5Key);
        Assert.isTrue(lastPassword.equalsIgnoreCase(admin.getPassword()), "密码错误");
        admin.setPassword(Encrypt.MD5(newPassword + md5Key));
        adminService.save(admin);
        return MessageResult.success("修改密码成功");
    }
}

