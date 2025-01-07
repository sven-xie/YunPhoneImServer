package com.sven.rtcim;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * @author sven
 * @date 2025/01/06
 **/
@SpringBootApplication
@ServletComponentScan
public class RtcImApplication {

    public static void main(String[] args) {
        SpringApplication.run(RtcImApplication.class, args);
    }

}
