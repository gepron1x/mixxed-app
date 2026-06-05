package org.gepron1x.mixxed.configuration;


import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.gepron1x.mixxed.entity.User;
import org.gepron1x.mixxed.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AdminConfig {

    @Value("${mixxed.admin.usernames}")
    private String usernames;

    private final UserRepository userRepository;

    public AdminConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @PostConstruct
    public void init() {
        System.out.println(usernames);
        for(String username : usernames.split(",")) {
            User user = userRepository.findByUsername(username).orElse(null);
            if(user == null) continue;
            System.out.println("ADMIN: " + username);
            if(user.isAdmin()) continue;
            user.setAdmin(true);
            userRepository.save(user);
        }
    }
}
