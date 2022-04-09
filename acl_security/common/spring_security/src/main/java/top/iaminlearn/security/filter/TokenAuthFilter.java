package top.iaminlearn.security.filter;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import top.iaminlearn.security.security.TokenManager;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Date: 2022/4/8 1:26
 */
public class TokenAuthFilter  extends BasicAuthenticationFilter {

    private TokenManager tokenManage;
    private RedisTemplate redisTemplate;

    public TokenAuthFilter(AuthenticationManager authenticationManager, TokenManager tokenManage, RedisTemplate redisTemplate) {
        super(authenticationManager);
        this.tokenManage = tokenManage;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 获取当前认证成功用户权限信息
        UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
        if (authentication != null) {
            // 将当前用户权限信息放入到上下文中
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        chain.doFilter(request, response); // 执行下一个过滤器
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        // 获取token
        String token = request.getHeader("token");
        if (token != null) {
            // 从token 中获取用户信息
            String username = tokenManage.getUserInfoFromToken(token);

            // 从redis中获取用户权限信息
            List<String> permissionsValueList = (List<String>) redisTemplate.opsForValue().get(username);
            Collection<GrantedAuthority> authority = new ArrayList<>();
            for (String permissionValue : permissionsValueList) {
                SimpleGrantedAuthority auth = new SimpleGrantedAuthority(permissionValue);
                authority.add(auth);
            }
            return new UsernamePasswordAuthenticationToken(username, token, authority);
        }
        return null;
    }
}
