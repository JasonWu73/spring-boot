package net.wuxianjie.jwt.service;

import net.wuxianjie.common.exception.AuthenticationException;
import net.wuxianjie.jwt.model.Token;
import net.wuxianjie.jwt.model.TokenData;

public interface AccessTokenService {

  Token getOrCreateToken(String username, String password) throws AuthenticationException;

  TokenData verifyToken(String token) throws AuthenticationException;

  Token refreshToken(String token) throws AuthenticationException;
}
