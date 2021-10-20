package com.senderman.lastkatkabot;

import com.senderman.lastkatkabot.service.CachingUserActivityTrackerService;
import com.senderman.lastkatkabot.service.ChatPolicyEnsuringService;
import org.springframework.stereotype.Component;

@Component
public class BackgoundServiceLauncher {

    private final CachingUserActivityTrackerService userActivityTrackerService;
    private final ChatPolicyEnsuringService chatPolicyEnsuringService;

    public BackgoundServiceLauncher(
            CachingUserActivityTrackerService userActivityTrackerService,
            ChatPolicyEnsuringService chatPolicyEnsuringService
    ) {
        this.userActivityTrackerService = userActivityTrackerService;
        this.chatPolicyEnsuringService = chatPolicyEnsuringService;
    }

    public void runServices() {
        userActivityTrackerService.runCacheListener();
        chatPolicyEnsuringService.runViolationChecker();
    }
}
