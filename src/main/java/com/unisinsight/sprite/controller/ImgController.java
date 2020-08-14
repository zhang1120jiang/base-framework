package com.unisinsight.sprite.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/sprite/img")
@Api(tags = "雪碧图")
public class ImgController {

    @GetMapping
    @ApiOperation("获取雪碧图")
    public String getImg(/*HttpServletResponse response, @RequestParam("file") MultipartFile file*/){
        return "aaaa";
    }
}
