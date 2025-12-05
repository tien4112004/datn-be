package com.datn.datnbe.cms.security.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.datn.datnbe.cms.repository.ClassRepository;
import com.datn.datnbe.sharedkernel.exceptions.AppException;
import com.datn.datnbe.sharedkernel.exceptions.ErrorCode;
import com.datn.datnbe.sharedkernel.security.annotation.RequireTeacherPermission;
import com.datn.datnbe.sharedkernel.security.utils.SecurityContextUtils;
import com.datn.datnbe.sharedkernel.security.aspect.ParamUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Order(1)
@Slf4j
@RequiredArgsConstructor
public class TeacherPermissionAspect {

    private final ClassRepository classRepository;

    private final SecurityContextUtils securityContextUtils;

    @Before("@annotation(com.datn.datnbe.sharedkernel.security.annotation.RequireTeacherPermission)")
    public void checkTeacherPermission(JoinPoint jp, RequireTeacherPermission requireTeacherPermission) {
        log.debug("Checking teacher permission for method: {}", jp.getSignature().getName());

        String classId = String.valueOf(ParamUtils.getParamValue(jp, "classId"));
        var userId = securityContextUtils.getCurrentUserId();
        if (!classRepository.isTheTeacherOfClass(classId, userId)) {
            log.debug("Teacher does not have permission for class: {}", classId);
            throw new AppException(ErrorCode.CLASS_NOT_FOUND, "Class not found or you do not have permission.");
        }

    }

}
