package com.wolf.inaction.beyondcallback;

import io.reactivex.Observable;

import java.util.concurrent.TimeUnit;

/**
 * Description:
 * Created on 2021/5/27 1:18 PM
 *
 * @author 李超
 * @version 0.0.1
 */
public class RxJavaTest {
    public static void main(String[] args) {
        //testFirst();
        //testErrorHandle();

        dealWithLifeCycle();
    }

    private static void dealWithLifeCycle() {
        Observable
                .just("--", "this", "is", "--", "a", "sequence", "of", "items", "!")
                .doOnSubscribe(d -> System.out.println("Subscribed!"))// action
                .delay(2, TimeUnit.SECONDS)// delays emitting events by five seconds
                .filter(s -> !s.startsWith("--"))
                .doOnNext(s -> System.out.println("doOnNext " + s))// another action,打印流中每个元素
                .map(String::toUpperCase)
                .buffer(2)// groups events 2 by 2
                .subscribe(System.out::println,
                        Throwable::printStackTrace,
                        () -> System.out.println(">>> Done"));// when the stream has completed
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void testErrorHandle() {
        Observable.<String>error(() -> new RuntimeException("Woops"))// emits one error
                .map(String::toUpperCase)// never called
                .subscribe(System.out::println, Throwable::printStackTrace);
    }

    private static void testFirst() {
        Observable.just(1, 2, 3)// an observable of a predefined sequence
                .map(Object::toString)
                .map(s -> "@" + s)
                .subscribe(System.out::println);
    }
}
