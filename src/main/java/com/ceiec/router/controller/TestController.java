package com.ceiec.router.controller;

import com.ceiec.router.dto.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * desc:
 *
 * @author : caokunliang
 * creat_date: 2019/11/4 0004
 * creat_time: 12:53
 **/
@RestController
@Slf4j
@RequestMapping("/migrate/")
public class TestController {

    /**
     * test
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/test")
    public Object test(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = new User();
        user.setUserName("愚公");
        user.setAge(20);
        log.info("result:{}",user);

        return user;
    }
}
