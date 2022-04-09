package top.iaminlearn.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import top.iaminlearn.security.entity.SecurityUser;
import top.iaminlearn.security.entity.User;
import top.iaminlearn.security.security.TokenManager;
import top.iaminlearn.utils.utils.R;
import top.iaminlearn.utils.utils.ResponseUtil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Date: 2022/4/8 0:08
 */
public class TokenLoginFilter  extends UsernamePasswordAuthenticationFilter {

    private TokenManager tokenManage;
    private RedisTemplate redisTemplate;
    private AuthenticationManager authenticationManager;

    public TokenLoginFilter(TokenManager tokenManage, RedisTemplate redisTemplate, AuthenticationManager authenticationManager) {
        this.tokenManage = tokenManage;
        this.redisTemplate = redisTemplate;
        this.authenticationManager = authenticationManager;
        this.setPostOnly(false); // 允许表单提交
        this.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/admin/acl/login","POST")); // 允许登录请求是POST方式
    }


    // 获取表单提交用户名和密码
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            User user = new ObjectMapper().readValue(request.getInputStream(), User.class);
            // 回去调用 userDetail获取用户信息，即去查询数据库
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword(),new ArrayList<>()));
        } catch (IOException e) {
            e.printStackTrace();
            throw  new RuntimeException();
        }
    }

    // 认证成功调用的方法
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        // 认证成功，得到认证成功之后的信息
        SecurityUser user = (SecurityUser) authResult.getPrincipal();
        // 根据用户名生成token
        String token = tokenManage.createToken(user.getCurrentUserInfo().getUsername());
        // 把用户名称和用户权限列表放到redis中
        redisTemplate.opsForValue().set(user.getCurrentUserInfo().getUsername(),user.getPermissionValueList());
        // 把token放到响应头中
        ResponseUtil.out(response, R.ok().data("token",token));
    }

    // 认证失败调用的方法
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        //认证失败 错误信息提示
        ResponseUtil.out(response, R.error());
    }
}
