package org.gepron1x.mixxed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class MixxedApplication {

    public static void main(String[] args) {
        SpringApplication.run(MixxedApplication.class, args);
    }


    @GetMapping
    public String helloWorld() {
        return "Hello world, Mixxed!";
    }

}
