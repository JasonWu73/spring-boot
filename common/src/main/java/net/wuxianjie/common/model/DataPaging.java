package net.wuxianjie.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataPaging<T> {

  /**
   * 总条数
   */
  private long total;

  /**
   * 当前页码
   */
  private int current;

  /**
   * 每页条数
   */
  private int size;

  /**
   * 数据列表项
   */
  private T list;
}
