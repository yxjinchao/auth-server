package com.landongnet.auth.service;

import com.github.snake.rock.common.exception.ValidateCodeException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author snake
 * @description
 * @since 2023/8/17 15:54
 */
public interface ValidateCodeService {

    /**
     * 生成验证码
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @throws java.io.IOException           IO异常
     * @throws com.github.snake.rock.common.exception.ValidateCodeException 验证码异常
     */
    void create(HttpServletRequest request, HttpServletResponse response) throws IOException, ValidateCodeException;

    /**
     * 校验验证码
     *
     * @param key   前端上送 key
     * @param value 前端上送待校验值
     * @throws com.github.snake.rock.common.exception.ValidateCodeException 验证码异常
     */
    void check(String key, String value) throws ValidateCodeException;
}
