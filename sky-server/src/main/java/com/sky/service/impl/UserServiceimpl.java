package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.JwtProperties;
import com.sky.properties.WeChatProperties;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import com.sky.vo.UserLoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceimpl implements UserService {
    //微信登录的url
    private static final String WECHAT_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";
    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;
    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    @Override
    public User wxlogin(UserLoginDTO userLoginDTO) {
        String result = getOpenId(userLoginDTO.getCode());
        JSONObject jsonObject = JSONObject.parseObject(result);

        String openid = jsonObject.getString("openid");
        if(openid == null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //看是否是新用户
        User user = userMapper.getByOpenId(openid);
        //如果是就注册
        if(user == null){
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
        //返回用户信息

        return user;
    }

    private String getOpenId(String code){
        Map<String, String> query = new HashMap<>();
        query.put("appid", weChatProperties.getAppid());
        query.put("secret", weChatProperties.getSecret());
        query.put("js_code", code);
        query.put("grant_type", "authorization_code");
        String result = HttpClientUtil.doGet(WECHAT_LOGIN_URL, query);
        return result;
    }
}
