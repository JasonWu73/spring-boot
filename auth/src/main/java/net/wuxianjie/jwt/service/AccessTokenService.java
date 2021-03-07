package net.wuxianjie.jwt.service;

import net.wuxianjie.common.exception.AuthenticationException;
import net.wuxianjie.jwt.model.CreateToken;
import net.wuxianjie.jwt.model.ParseToken;

public interface AccessTokenService {

  CreateToken getOrCreateToken(String username, String password) throws AuthenticationException;

  ParseToken verifyToken(String token) throws AuthenticationException;
}
