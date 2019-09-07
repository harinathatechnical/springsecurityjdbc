package com.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeResource {

    /**
     * this is able to access by everyone
     * @return
     */
    @GetMapping("/")
    public String home(){
        return ("<h1>Welcome</h1>");
    }

    /**
     * this is able to access by only admin
     * @return
     */
    @GetMapping("/admin")
    public String admin(){
        return ("<h1>Welcome Admin</h1>");
    }

    /**
     * this is able to access by admin and user
     * @return
     */
    @GetMapping("/user")
    public String user(){
        return ("<h1>Welcome User</h1>");
    }
}
