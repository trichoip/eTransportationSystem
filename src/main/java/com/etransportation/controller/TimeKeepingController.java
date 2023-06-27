package com.etransportation.controller;

import com.etransportation.service.TimeKeepingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/timekeeping")
@RequiredArgsConstructor
public class TimeKeepingController {

    private final TimeKeepingService timeKeepingService;

    @PostMapping
    @Operation(tags = "timekeeping", security = @SecurityRequirement(name = "token_auth"))
    public ResponseEntity<?> timeKeeping(
        @Schema(allowableValues = { "checkin", "checkout" }) @RequestParam(required = true) String type,
        @RequestParam(required = true) Long schedulesId,
        Authentication authentication
    ) {
        Object attendance = timeKeepingService.attendance(type, schedulesId, authentication);
        ResponseEntity<?> responseEntity = (ResponseEntity<?>) attendance;
        return new ResponseEntity<>(responseEntity.getBody(), responseEntity.getStatusCode());
    }
}
