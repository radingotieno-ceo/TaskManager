package com.taskmanager.service;

import com.taskmanager.entity.Role;
import com.taskmanager.entity.User;
import com.taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class UserManagementService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getManagers() {
        return userRepository.findByRole(Role.MANAGER);
    }

    public List<User> getUsersAvailableForTaskAssignment() {
        return userRepository.findUsersAvailableForTaskAssignment();
    }

    public void updateUserRole(Long userId, Role newRole) {
        userRepository.updateUserRole(userId, newRole);
    }

    public void promoteUserToManager(Long userId) {
        userRepository.updateUserRole(userId, Role.MANAGER);
    }

    public void demoteManagerToUser(Long userId) {
        userRepository.updateUserRole(userId, Role.USER);
    }

    public Map<String, Object> getUserStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        statistics.put("totalUsers", userRepository.countTotalUsers());
        statistics.put("adminUsers", userRepository.countByRole(Role.ADMIN));
        statistics.put("managerUsers", userRepository.countByRole(Role.MANAGER));
        statistics.put("regularUsers", userRepository.countByRole(Role.USER));
        
        List<Object[]> roleStats = userRepository.getUserStatisticsByRole();
        Map<String, Long> roleBreakdown = new HashMap<>();
        for (Object[] stat : roleStats) {
            Role role = (Role) stat[0];
            Long count = (Long) stat[1];
            roleBreakdown.put(role.name(), count);
        }
        statistics.put("roleBreakdown", roleBreakdown);
        
        return statistics;
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
