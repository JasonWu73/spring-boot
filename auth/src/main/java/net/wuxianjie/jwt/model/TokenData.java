package net.wuxianjie.jwt.model;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenData {

  private String username;

  private String type;

  private Date nbf;
}
