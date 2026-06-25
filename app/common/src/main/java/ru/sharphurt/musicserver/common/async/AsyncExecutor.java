package ru.sharphurt.musicserver.common.async;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AsyncExecutor {

    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;

    public <I, O> List<O> callForMultipleArgumentsAsync(List<I> arguments,
        Function<I, O> callFunction) {
        if (arguments == null || arguments.isEmpty()) {
            return List.of();
        }
        log.info(
            "BEFORE ALL pool={}, active={}, queue={}",
            threadPoolTaskExecutor.getPoolSize(),
            threadPoolTaskExecutor.getActiveCount(),
            threadPoolTaskExecutor.getQueueSize()
        );

        List<CompletableFuture<O>> futures = arguments.stream()
            .map(arg ->
                CompletableFuture.supplyAsync(
                        () -> callFunction.apply(arg),
                        threadPoolTaskExecutor
                    )
                    .orTimeout(10, TimeUnit.SECONDS)
                    .exceptionally(ex -> {
                        log.error("Task failed", ex);
                        return null;
                    })
            )
            .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.info(
            "AFTER ALL pool={}, active={}, queue={}",
            threadPoolTaskExecutor.getPoolSize(),
            threadPoolTaskExecutor.getActiveCount(),
            threadPoolTaskExecutor.getQueueSize()
        );
        return futures.stream()
            .map(CompletableFuture::join)
            .filter(Objects::nonNull)
            .toList();
    }
}
