package com.weiki.usercenter.controller;

import com.weiki.usercenter.dto.CaptchaVO;
import com.weiki.usercenter.entity.Result;
import com.weiki.usercenter.service.RedisCaptchaService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 验证码相关接口控制器
 */
@Api(tags = "验证码")
@RestController
@RequestMapping("/captcha")
@Slf4j
public class CaptchaController {

    private final RedisCaptchaService redisCaptchaService;

    public CaptchaController(RedisCaptchaService redisCaptchaService) {
        this.redisCaptchaService = redisCaptchaService;
    }

    /**
     * 获取验证码图片
     *
     * @return 验证码ID和Base64图片
     */
    @GetMapping("/generate")
    @ApiOperation("获取验证码图片")
    public Result<CaptchaVO> generateCaptcha() {
        // 一次性获取验证码ID和图片，避免重复生成
        RedisCaptchaService.CaptchaResult result = redisCaptchaService.generateCaptcha();

        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setCaptchaId(result.getCaptchaId());
        captchaVO.setCaptchaImage("data:image/png;base64," + result.getCaptchaImage());

        log.debug("验证码已生成，ID：{}", result.getCaptchaId());
        return Result.success("验证码生成成功", captchaVO);
    }
}


