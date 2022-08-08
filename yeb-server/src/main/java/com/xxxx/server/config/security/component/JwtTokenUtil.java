package com.xxxx.server.config.security.component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenUtil {
    //准备两个常量，用于存储信息
    private static final String CLAIM_KEY_USERNAME = "sub";     //用于将用户名放入荷载中
    private static final String CLAIM_KEY_CREATED = "created";      //用于将Jwt创建时间放入荷载中
    @Value("${jwt.secret}")     //密钥通过Value注解去配置目录中得到
    private String secret;
    @Value("${jwt.expiration}")     //失效时间通过Value注解去配置目录中得到
    private Long expiration;

    /**
     * 根据用户信息生成token
     *
     * @param userDetails
     * @return
     */
    public String generateToken(UserDetails userDetails) {      //从spring-security中获取用户信息
        Map<String, Object> claims = new HashMap<>();       //创建一个Map，作为JWT的荷载，荷载中存放从UserDetails获取的用户名和创建时间
        claims.put(CLAIM_KEY_USERNAME, userDetails.getUsername());
        claims.put(CLAIM_KEY_CREATED, new Date());
        return generateToken(claims);       //根据Map中的信息生成token
    }

    /**
     * 从token中获取登录用户名
     * @param token
     * @return
     */
    public String getUserNameFromToken(String token){       //用户名保存在Map键值对荷载中，从荷载中拿用户名
        String username;
        try {
            Claims claims = getClaimsFormToken(token);      //从token中获取荷载
            username = claims.getSubject();     //从荷载中获取用户名
        } catch (Exception e) {
            username = null;
        }
        return username;
    }

    /**
     * 验证token是否有效
     * @param token
     * @param userDetails
     * @return
     */
    public boolean validateToken(String token,UserDetails userDetails){
        String username = getUserNameFromToken(token);      //从token中获取用户名
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);        //判断当前token中的用户名是否和spring-security中获取用户名是否相同以及token是否失效
    }

    /**
     * 判断token是否可以被刷新
     * @param token
     * @return
     */
    public boolean canRefresh(String token){        //判断token是否可以刷新
        return !isTokenExpired(token);      //根据token过期时间与当前时间进行比较，判断token是否可一被刷新
    }

    /**
     * 刷新token
     * @param token
     * @return
     */
    public String refreshToken(String token){
        Claims claims = getClaimsFormToken(token);      //从token中获取荷载
        claims.put(CLAIM_KEY_CREATED,new Date());       //通过传入当前时间，更新荷载中的过期时间
        return generateToken(claims);       //根据更新后的荷载，重新生成token
    }

    /**
     * 判断token是否失效
     * @param token
     * @return
     */
    private boolean isTokenExpired(String token) {      //判断是否失效
        Date expireDate = getExpiredDateFromToken(token);       //从token中获得失效时间
        return expireDate.before(new Date());       //判断失效时间是否在当前时间之前
    }

    /**
     * 从token中获取过期时间
     * @param token
     * @return
     */
    private Date getExpiredDateFromToken(String token) {
        Claims claims = getClaimsFormToken(token);      //从token中获取荷载
        return claims.getExpiration();      //获取荷载中的过期时间
    }

    /**
     * 从token中获取荷载
     * @param token
     * @return
     */
    private Claims getClaimsFormToken(String token) {
        Claims claims = null;       //为避免异常，先令荷载为空
        try {
            claims = Jwts.parser()      // 传入参数，获取荷载
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return claims;
    }

    /**
     * 根据荷载生成JWT TOKEN
     *
     * @param claims
     * @return
     */
    private String generateToken(Map<String, Object> claims) {      //传入荷载Map键值对，根据荷载生成token
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(generateExpirationDate())        //传入失效时间，定义失效时间为lone类型，故通过方法转换成Date类型
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    private Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + expiration * 1000);        //将失效时间从long类型转换成Date类型
    }

}
