package com.cognizant.healthcaregov.aspect;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditAspect {
    @Autowired
    LogService logService;

    @Pointcut("execution(* com.cognizant.healthcaregov.service.HospitalService.*(..))")
    public void hospitalServiceMethods(){}

    @Pointcut("execution(* com.cognizant.healthcaregov.service.AppointmentService.*(..))")
    public void appointmentServiceMethods(){}

    @Pointcut("execution(* com.cognizant.healthcaregov.service.ResourceService.*(..))")
    public void resourceServiceMethods(){}


    @Pointcut("execution(* com.cognizant.healthcaregov.service.ComplianceService.*(..))")
    public void complianceServiceMethods(){}

    @Pointcut("execution(* com.cognizant.healthcaregov.service.PatientService.*(..))")
    public void patientServiceMethods(){}

    @Pointcut("execution(* com.cognizant.healthcaregov.service.NotificationService.*(..))")
    public void notificationServiceMethods(){}

    @Pointcut("execution(* com.cognizant.healthcaregov.service.ProgramService.*(..))")
    public void programServiceMethods(){}

    @Pointcut("execution(* com.cognizant.healthcaregov.service.ScheduleService.*(..))")
    public void scheduleServiceMethods(){}

    @Pointcut("execution(* com.cognizant.healthcaregov.service.TreatmentService.*(..))")
    public void treatmentServiceMethods(){}

    @Pointcut("execution(* com.cognizant.healthcaregov.service.TreatmentService.*(..))")
    public void userServiceMethods(){}

    @Pointcut("execution(* com.cognizant.healthcaregov.service.AuditService.*(..))")
    public void auditServiceMethods(){}

    @Pointcut("execution(* com.cognizant.healthcaregov.service.AnalyticsService.*(..))")
    public void analyticsServiceMethods(){}

    @Around("hospitalServiceMethods() || appointmentServiceMethods() || resourceServiceMethods() || complianceServiceMethods() || patientServiceMethods() || notificationServiceMethods() || programServiceMethods() || treatmentServiceMethods() || userServiceMethods() || auditServiceMethods() || analyticsServiceMethods(){}")
    public Object logAudit(ProceedingJoinPoint jp) throws Throwable
    {
        String methodName=jp.getSignature().getName();
        String resource=jp.getTarget().getClass().getSimpleName().replace("Service","").replace("$$SpringCGLIB$$0","");
        String username= SecurityContextHolder.getContext().getAuthentication().getName();
        String action=deriveAction(methodName);
        Object result=null;
        try{
            result=jp.proceed();

        }
        catch(Exception e)
        {
            if(!methodName.equals("register"))
                logService.savelog(username,action+"_FAILED",resource);
            throw e;
        }
        if(!methodName.equals("register"))
            logService.savelog(username,action,resource);
        return result;
    }
    public String deriveAction(String methodName)
    {
        switch (methodName)
        {
            case "create": return "CREATE";
            case "getAll": return "VIEW_ALL";
            case "getById": return "VIEW";
            case "search": return "SEARCH";
            case "update": return "UPDATE";
            case "delete": return "DELETE";

            case "bookAppointment": return "BOOK";
            case "cancelAppointment": return "CANCEL";
            case "getByDoctor": return "VIEW_DOCTOR";
            case "reassign": return "UPDATE";
            case "findById": return "VIEW";

            case "add": return "CREATE";
            case "sumByType": return "COUNT";


            case "getAuditLogs": return "VIEW_AUDITLOGS";

            case "getProfile": return "VIEW";
            case "updateProfile": return "UPDATE";

            case "send": return "SEND";
            case "getForUser": return "VIEW_NOTIFICATIONS";

            case "getDashboard": return "VIEW_DASHBOARD";
            case "generateReport": return "GENERATE_REPORT";


            case "findSlot": return "FIND_SLOT";
            case "saveSlot": return "SAVE_SLOT";

            case "record": return "RECORD";
            case "updatePatientRecord": return "UPDATE_RECORD";

            case "getUsers": return "VIEW_ALL";
            case "updateStatus": return "UPDATE_STATUS";

            case "getHospitalAnalytics": return "GET_HOSPITAL";
            case "getCapacityReport": return "GET_CAPACITY";

            default: return methodName.toUpperCase();
        }
    }


}
