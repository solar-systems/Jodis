package cn.abelib.jodis.impl;

/**
 * @author abel.huang
 * @date 2020/6/30 17:42
 */
public class JodisString {
   private String holder;

   public JodisString(){}

   public JodisString(String holder) {
       this.holder = holder;
   }

    public String getHolder() {
        return this.holder;
    }

    public int size() {
        return this.holder.length();
    }
}
