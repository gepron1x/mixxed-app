package org.gepron1x.mixxed.configuration;


import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gepron1x.mixxed.entity.User;
import org.gepron1x.mixxed.repository.UserRepository;
import org.gepron1x.mixxed.util.MixxedDemoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AdminConfig {

    @Value("${mixxed.admin.usernames}")
    private String usernames;

    @Value("${mixxed.populate.demo.data}")
    private boolean populateDemoData;

    private final UserRepository userRepository;
    private final MixxedDemoService mixxedDemoService;

    public AdminConfig(UserRepository userRepository, MixxedDemoService mixxedDemoService) {
        this.userRepository = userRepository;
        this.mixxedDemoService = mixxedDemoService;
    }


    @PostConstruct
    public void init() {
        for(String username : usernames.split(",")) {
            User user = userRepository.findByUsername(username).orElse(null);
            if(user == null) continue;
            log.info("Marked {} as admin", user.getUsername());
            if(user.isAdmin()) continue;
            user.setAdmin(true);
            userRepository.save(user);
        }
        mixxedDemoService.dropAll();
        if(populateDemoData) {
            log.info("Demonstartion mode enabled.");
            mixxedDemoService.buildDemonstrationEntities();
        }
    }
}
