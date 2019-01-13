package com.bulingbuu.bubprotocol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BubProtocolApplication {

    public static void main(String[] args) {
        SpringApplication.run(BubProtocolApplication.class, args);
        new Server().bind();
    }

}

