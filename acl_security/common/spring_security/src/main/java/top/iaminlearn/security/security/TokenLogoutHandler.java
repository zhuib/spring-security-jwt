package top.iaminlearn.security.security;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import top.iaminlearn.utils.utils.R;
import top.iaminlearn.utils.utils.ResponseUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Date: 2022/4/7 23:51
 * 退出处理
 */

public class TokenLogoutHandler implements LogoutHandler {

    private TokenManager tokenManager;
    private RedisTemplate redisTemplate;


    public TokenLogoutHandler(TokenManager tokenManager, RedisTemplate redisTemplate) {
        this.tokenManager = tokenManager;
        this.redisTemplate = redisTemplate;
    }


    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // 1 从 header 获取 token
        //2 token 不为空，移除 token，并且移除 redis 中的 token

        String token = request.getHeader("token");
        if (token != null) {
            // 移除 token
            tokenManager.removeToken(token);

            // 移除 redis 中的 token
            String username = tokenManager.getUserInfoFromToken(token);
            redisTemplate.delete(username);
        }
        ResponseUtil.out(response, R.ok());
    }
}
