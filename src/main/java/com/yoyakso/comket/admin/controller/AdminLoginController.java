package com.yoyakso.comket.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AdminLoginController {

    /**
     * 관리자 로그인 페이지
     */
    @GetMapping("/admin-login")
    public String adminLoginPage() {
        return "admin-login";
    }

    /**
     * 관리자 리다이렉트 페이지
     */
    @GetMapping("/admin-redirect")
    public String adminRedirectPage() {
        return "admin-redirect";
    }
}
