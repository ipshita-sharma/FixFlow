package com.fixflow.controller;

import com.fixflow.dao.NotificationDao;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationDao notifications;
    public NotificationController(NotificationDao notifications) { this.notifications = notifications; }
    @GetMapping public List<Map<String, Object>> list(@RequestParam(defaultValue = "1") long userId) { return notifications.findForUser(userId); }
    @PutMapping("/{id}/read") @ResponseStatus(HttpStatus.NO_CONTENT) public void markRead(@PathVariable long id, @RequestParam(defaultValue = "1") long userId) { notifications.markRead(id, userId); }
}
