package com.nutriflow.helpers;

import com.nutriflow.entities.MedicalFileEntity;
import com.nutriflow.entities.UserEntity;
import com.nutriflow.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.nutriflow.exceptions.FileAccessDeniedException;

@Component
@RequiredArgsConstructor
public class MedicalFileAccessHelper {

    private final EntityFinderHelper entityFinderHelper;

    public MedicalFileEntity resolveMedicalFile(Long fileId, UserDetails userDetails) {
        SecurityUser securityUser = (SecurityUser) userDetails;

        if (securityUser.isUser()) {
            UserEntity user = entityFinderHelper.findUserByEmail(securityUser.getUsername());
            Long healthProfileId = entityFinderHelper.findHealthProfileByUser(user).getId();
            return entityFinderHelper.findMedicalFileByIdAndHealthProfileId(fileId, healthProfileId);
        }

        if (securityUser.isDietitian() || securityUser.isAdmin()) {
            return entityFinderHelper.findMedicalFileById(fileId);
        }

        throw new FileAccessDeniedException("You do not have permission to access this file.");
    }


}
