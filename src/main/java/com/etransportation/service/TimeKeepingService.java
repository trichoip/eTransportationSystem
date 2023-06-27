package com.etransportation.service;

import org.springframework.security.core.Authentication;

public interface TimeKeepingService {
    Object attendance(String type, Long schedulesId, Authentication authentication);
}
