package cn.abelib.jodis.example;

import cn.abelib.jodis.Jodis;

import java.util.stream.IntStream;

/**
 * @author abel.huang
 * @date 2020/6/30 18:36
 */
public class JodisExample {
    public static void main(String[] args) {
        Jodis jodis = new Jodis();

        IntStream.rangeClosed(1, 1000).forEach(
                i -> jodis.put(String.valueOf(i) , String.valueOf(i << 1))
        );

        IntStream.rangeClosed(1, 1000).forEach(
                i -> System.err.println(i + ": "  + jodis.get(String.valueOf(i)))
        );

        System.out.println(jodis.size());
    }
}
