package armeria.lecture.week1;

import static org.awaitility.Awaitility.await;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;

class FutureAndThreadTest {

    @Test
    void simpleCallback() {
        currentThreadName();
        final CompletableFuture<String> future = new CompletableFuture<>();
        future.thenAccept(str -> {
            System.err.println("Hello " + str);
            currentThreadName();
        });

        future.complete("Armeria");
    }

    @Test
    void futureGet() throws Exception {
        currentThreadName();
        final CompletableFuture<String> future = new CompletableFuture<>();
        // Never complete
        future.get();

        future.complete("Armeria");
    }

    @Test
    void completeByAnotherThread() {
        currentThreadName();
        final CompletableFuture<String> future = new CompletableFuture<>();
        future.thenAccept(str -> {
            System.err.println("Hello " + str);
            currentThreadName();
        });

        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            future.complete("foo");
        });

        await().until(future::isDone);
    }

    @Test
    void completeByAnotherThread2() {
        currentThreadName();
        final CompletableFuture<String> future = new CompletableFuture<>();
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        // 1 (main)
        executor.submit(() -> {
            // 3 (executor)
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Delay가 있었다면 executor에 의하여 실행되었을 것
            future.complete("foo");
        });

        // future is complete
        // 이미 complete 된 future에 callback을 달면 그 callback을 달아버린 main thread가 실행
        // 2 (main)
        future.thenAccept(str -> {
            // 4 (executor), 1000 ms 대기타지 않으면 main thread가 실행
            System.err.println("Hello " + str);
            currentThreadName();
        });

        // future callback을 붙였을 때
        // 1 - future가 incomplete -> future를 complete 시킨 thread가 모든 callback을 실행
        // 2 - future가 complete -> callback을 붙인 thread가 callback까지 실행

        await().until(future::isDone);


        // async로 끝나는 메소드들은 complete되면 callback을 실행시킬 thread를 직접 지정
    }

    @Test
    void completeByAnotherThread_completeFirst() {
        currentThreadName();
        final CompletableFuture<String> future = new CompletableFuture<>();
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        // future는 한번만 complete되고, 내부적으로 처리 stack을 가지고 있음
        /*
        executor.submit(() -> {
            future.complete("foo");
        });
         */

        future.thenAccept(str -> {
            System.out.println("Printed last");
            currentThreadName();
        });

        future.handle((str, cause) -> {
            System.out.println("Printed first");
            return null;
        });

        // complete이 안되면 stack에 넣는 것이고, complete이 된 상태라면 바로바로 실행함
        future.complete("foo");

        await().until(future::isDone);
    }

    @Test
    void completeByAnotherThread_completeFirst2() {
        currentThreadName();
        final CompletableFuture<String> future = new CompletableFuture<>();
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        // future는 한번만 complete되고, 내부적으로 처리 stack을 가지고 있음
        executor.submit(() -> {
            future.complete("foo");
        });

        future.thenAccept(str -> {
            System.out.println("Printed last");
            currentThreadName();
        });

        future.handle((str, cause) -> {
            System.out.println("Printed first");
            return null;
        });

        // complete이 안되면 stack에 넣는 것이고, complete이 된 상태라면 바로바로 실행함

        await().until(future::isDone);
    }

    @Test
    void completeByAnotherThread_thenAcceptAsync() {
        currentThreadName();
        final CompletableFuture<String> future = new CompletableFuture<>();
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        future.thenAcceptAsync(str -> {
            currentThreadName();
        }, executor);

        executor.submit(() -> {
            future.complete("foo");
        });

        await().until(future::isDone);
    }

    @Test
    void completeByAnotherThread_thenAcceptAsync2() {
        currentThreadName();
        final CompletableFuture<String> future = new CompletableFuture<>();
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        future.thenAcceptAsync(str -> {
            currentThreadName();
        }, executor);

        // Complete은 main에서 했지만, async method로 callback을 달았으므로 executor에 의해 callback이 실행됨
        future.complete("foo");

        await().until(future::isDone);
    }

    @Test
    void completeByAnotherThread_thenAcceptAsync_forkJoin() {
        currentThreadName();
        final CompletableFuture<String> future = new CompletableFuture<>();
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        future.thenAcceptAsync(str -> {
            currentThreadName();
        });

        executor.submit(() -> {
            future.complete("foo");
        });

        await().until(future::isDone);
    }

    static void currentThreadName() {
        System.err.println("Name: " + Thread.currentThread().getName());
    }
}
