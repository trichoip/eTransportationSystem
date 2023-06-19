package com.etransportation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Getter
@Component
public class ApplicationProperties {

  @Value("${application.security.authentication.jwt.access-token.secret-key}")
  private String accessSecretKey;

  @Value("${application.security.authentication.jwt.access-token.expire-time}")
  private Long accessExpireTime;

  @Value("${application.security.authentication.jwt.refresh-token.secret-key}")
  private String refreshSecretKey;

  @Value("${application.security.authentication.jwt.refresh-token.expire-time}")
  private Long refreshExpireTime;
}
