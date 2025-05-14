package org.example.bank.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class ScheduledTaskService {

    private final UserService userService;

    @Autowired
    public ScheduledTaskService(UserService userService) {
        this.userService = userService;
    }

    // Run every hour to check for and process expired blocks
    @Scheduled(fixedRate = 3600000)
    public void processExpiredBlocks() {
        userService.processExpiredBlocks();
    }
} 