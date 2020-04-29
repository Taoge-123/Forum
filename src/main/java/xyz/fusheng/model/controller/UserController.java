package xyz.fusheng.model.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import xyz.fusheng.model.common.utils.Result;
import xyz.fusheng.model.common.utils.StringUtils;
import xyz.fusheng.model.core.entity.Menu;
import xyz.fusheng.model.core.entity.User;
import xyz.fusheng.model.core.entity.UserRole;
import xyz.fusheng.model.core.service.MenuService;
import xyz.fusheng.model.core.service.RoleService;
import xyz.fusheng.model.core.service.UserRoleService;
import xyz.fusheng.model.core.service.UserService;
import xyz.fusheng.model.security.entity.SelfUser;

import java.util.List;

/**
 * 普通用户
 * @author code-fusheng
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private MenuService menuService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRoleService userRoleService;

    /**
     * 用户端信息
     * @Return Result<Object> 用户端信息
     */
    @GetMapping("/info")
    public Result<Object> userLogin(){
        SelfUser userDetails = (SelfUser) SecurityContextHolder.getContext().getAuthentication() .getPrincipal();
        return new Result<>("操作成功: 用户端信息！", userDetails);
    }

    /**
     * 拥有USER角色和sys:user:info权限可以访问
     * @Return Result<List<Menu>> 权限列表
     */
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/menuList")
    public Result<List<Menu>> menu(){
        List<Menu> menuList = menuService.list();
        return new Result<>("操作成功: 权限列表！",menuList);
    }

    /**
     * 注册用户 Security 开放接口
     * @param user
     * @return
     */
    @PostMapping("/register")
    public Result<Object> register(@RequestBody User user){
        if(StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPassword())){
            return new Result<>(400, "操作错误: 缺少必须表单字段！");
        }
        if(userService.selectUserByName(user.getUsername())!=null){
            return new Result<>(400, "操作错误: 该用户名已被注册！");
        }
        // 加密
        String encryptPass = new BCryptPasswordEncoder().encode(user.getPassword());
        user.setPassword(encryptPass);
        user.setStatus("NORMAL");
        Boolean ret = userService.save(user);
        if(!ret){
            return new Result<>("注册失败！");
        }
        // 默认角色
        UserRole userRole = new UserRole();
        userRole.setUserId(userService.selectUserByName(user.getUsername()).getUserId());
        userRole.setRoleId(1L);
        userRoleService.save(userRole);
        return new Result<>("注册成功！");
    }

}