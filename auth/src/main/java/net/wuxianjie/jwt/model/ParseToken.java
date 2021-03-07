package net.wuxianjie.jwt.model;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParseToken {

  private String username;

  private Date nbf;

  private Date exp;
}
