package com.cognizant.healthcaregov.aspect;

import com.cognizant.healthcaregov.dao.AuditLogRepository;
import com.cognizant.healthcaregov.dao.UserRepository;
import com.cognizant.healthcaregov.entity.AuditLog;
import com.cognizant.healthcaregov.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LogService {
    @Autowired
    private AuditLogRepository auditLogRepository;
    @Autowired
    private UserRepository userRepository;
    @Transactional(propagation= Propagation.REQUIRES_NEW)
    public void savelog(String username,String action,String resource)
    {
        AuditLog log=new AuditLog();
        log.setAction(action);
        System.out.println("pateitn regish i");
        User user=userRepository.findByEmail(username).orElseThrow(()-> new RuntimeException("Email not found"));
        log.setUser(user);
        log.setResource(resource);
        auditLogRepository.save(log);
    }
}
