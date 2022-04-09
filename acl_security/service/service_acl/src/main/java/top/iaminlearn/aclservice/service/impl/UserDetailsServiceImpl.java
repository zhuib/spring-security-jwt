package top.iaminlearn.aclservice.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import top.iaminlearn.aclservice.entity.User;
import top.iaminlearn.aclservice.service.PermissionService;
import top.iaminlearn.aclservice.service.UserService;
import top.iaminlearn.security.entity.SecurityUser;

import java.util.List;

/**
 * Date: 2022/4/8 15:37
 */

@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Autowired
    private PermissionService permissionService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 根据用户名查询数据
        User user = userService.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户名不存在");
        }
        top.iaminlearn.security.entity.User curUser = new top.iaminlearn.security.entity.User();
        BeanUtils.copyProperties(user, curUser);

        // 根据用户查询用户权限列表
        List<String> permissionValueList = permissionService.selectPermissionValueByUserId(user.getId());
        SecurityUser securityUser = new SecurityUser();
        securityUser.setCurrentUserInfo(curUser);
        securityUser.setPermissionValueList(permissionValueList);
        return securityUser;
    }
}
