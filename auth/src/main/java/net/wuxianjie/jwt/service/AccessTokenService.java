package net.wuxianjie.jwt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.wuxianjie.common.exception.AuthenticationException;
import net.wuxianjie.common.exception.BadRequestException;
import net.wuxianjie.jwt.model.Token;
import net.wuxianjie.jwt.model.TokenData;

public interface AccessTokenService {

  Token getOrCreateToken(String username, String password) throws AuthenticationException, JsonProcessingException;

  Token refreshToken(String token) throws AuthenticationException, BadRequestException, JsonProcessingException;

  TokenData verifyToken(String token) throws AuthenticationException, JsonProcessingException;
}
