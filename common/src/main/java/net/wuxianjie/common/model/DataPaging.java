package net.wuxianjie.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataPaging<T> {

  private long total;
  private int current;
  private int size;
  private T list;
}
