package top.iaminlearn.security.security;

import io.jsonwebtoken.CompressionCodecs;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Date: 2022/4/7 23:27
 */

@Component
public class TokenManager {
    // token 有效时长
    private static final long TOKEN_EXPiRE = 24 * 60 * 60 * 1000;
    // 编码密钥
    private static final String TOKEN_SIGN_KEY = "iaminlearn";

    // 使用 jwt 根据用户名生成token
    public String createToken(String username) {
        String token = Jwts.builder().setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_EXPiRE))
                .signWith(SignatureAlgorithm.HS512, TOKEN_SIGN_KEY)
                .compressWith(CompressionCodecs.GZIP).compact();

        return token;
    }

    // 根据 token 字符串获得用户信息
    public String getUserInfoFromToken(String token) {
        return Jwts.parser().setSigningKey(TOKEN_SIGN_KEY).parseClaimsJws(token).getBody().getSubject();
    }

    // 移除token
    public void removeToken(String token) {
        // TODO
    }
}
