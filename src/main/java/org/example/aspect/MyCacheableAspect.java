package org.example.aspect;

import cn.hutool.crypto.digest.DigestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.annotate.MyCacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.HttpStatus.LOCKED;

@Aspect
@Slf4j
@Component
public class MyCacheableAspect {

    private final RedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    public MyCacheableAspect(RedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(org.example.annotate.MyCacheable)")
    public Object getCache(ProceedingJoinPoint joinPoint) {
        log.info("进入Cacheable切面");
        // 获取方法参数
        Object[] arguments = joinPoint.getArgs();
        // 获取方法签名
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        // 获取方法名
        String methodName = method.getName();
        // 获取注解
        MyCacheable annotation = method.getAnnotation(MyCacheable.class);
        // 获取注解的值
        String invoker = annotation.invoker();
        // 获取digest，即redis的key
        log.info("Digest准备生成：methodName：" + methodName + "，invoker=" + invoker);
        String digest = generateDigest(methodName, invoker, arguments);
        log.info("Digest生成：digest=" + digest);
        Object redisValue = redisTemplate.opsForValue().get(digest);

        if (redisValue == null) {
            log.info("缓存未命中：" + digest);
            log.info("缓存刷新开始：" + digest);
            String expire = annotation.expire();
            String expireAt = annotation.expireAt();

            redisValue = executeSynOperate(result -> {
                        if (!result) {
                            log.error("分布式锁异常");
                            return null;
                        }
                        Object checkGet = redisTemplate.opsForValue().get(digest);
                        if (checkGet != null) {
                            return checkGet;
                        }
                        // 刷新缓存
                        refreshCache(joinPoint, digest, expire, expireAt);

                        if (method.getAnnotation(PostMapping.class) != null) {
                            redisTemplate.opsForSet().add(methodName, arguments);
                        }
                        return redisTemplate.opsForValue().get(digest);
                    }, digest + "1", 50000
            );
        }

        log.info("Cache返回：digest=" + digest);

        return redisValue;
    }

    private void refreshCache(ProceedingJoinPoint joinPoint, String key, String expire, String expireAt) {
        Object methodResult = null;

        try {
            // 放行，让切面捕获的任务继续执行并获取返回结果
            methodResult = joinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        if (methodResult == null) {
            methodResult = new Object();
        }

        // 如果注解传入的expire参数不为空，则直接设置过期时间，否则看expireAt是否为空，否则设置默认过期时间
        if (!expire.equals("")) {
            long expireLong = Long.parseLong(expire);
            redisTemplate.opsForValue().set(key, methodResult, expireLong, TimeUnit.SECONDS);
        } else if (!expireAt.equals("")) {
            LocalTime expireAtTime = LocalTime.parse(expireAt);
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expireDateTime = LocalDateTime.of(now.toLocalDate(), expireAtTime);

            if (expireDateTime.compareTo(now) <= 0) {
                expireDateTime = expireDateTime.plusDays(1);
            }
            redisTemplate.opsForValue().set(key, methodResult, Duration.between(now, expireDateTime));
        } else {
            redisTemplate.opsForValue().set(key, methodResult, 3600 * 12, TimeUnit.SECONDS);
        }
    }

    // 生成digest，用来当做redis中的key
    private String generateDigest(String methodName, String invoker, Object[] arguments) {
        String argumentsDigest = "";

        if (arguments != null && arguments.length > 0) {
            StringBuilder stringBuilder = new StringBuilder();

            for (Object argument : arguments) {
                try {
                    String valueAsString = objectMapper.writeValueAsString(argument);

                    stringBuilder.append(valueAsString);
                } catch (JsonProcessingException e) {
                    log.error("参数" + argument + "字符串处理失败", e);
                }
            }

            byte[] bytes = DigestUtil.md5(stringBuilder.toString());
            argumentsDigest = new String(bytes);
        }

        return methodName + (invoker == null ? "" : invoker) + argumentsDigest;
    }

    // 等待时间重复获取
    private <T> T executeSynOperate(MainOperator<T> operator, String lockCacheKey, long milliTimeout) {
        try {
            if (operator != null && lockCacheKey != null && milliTimeout >= 0L) {
                boolean locked = false;
                long startNano = System.nanoTime();
                boolean waitFlag = milliTimeout > 0L;
                long nanoTimeOut = (waitFlag ? milliTimeout : 50L) * 1000000L;
                T resultObj = null;

                try {
                    while (System.nanoTime() - startNano < nanoTimeOut) {
                        if (redisTemplate.opsForValue().setIfAbsent(lockCacheKey, LOCKED, 120L, TimeUnit.SECONDS)) {
                            locked = true;
                            break;
                        }

                        if (!waitFlag) {
                            break;
                        }

                        Thread.sleep(1000);
                    }

                    resultObj = operator.executeInvokeLogic(locked);
                } catch (Exception ex) {
                    log.error("处理逻辑", ex);
                    return null;
                } finally {
                    if (locked) {
                        releaseRedisLock(lockCacheKey);
                    }
                }

                return resultObj;
            } else {
                throw new Exception("参数不合法");
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 释放锁
     *
     * @param cacheKey
     */
    public boolean releaseRedisLock(final String cacheKey) {
        Boolean deleteLock = redisTemplate.delete(cacheKey);

        if (Boolean.TRUE.equals(deleteLock)) {
            return true;
        }

        return false;
    }

    // 函数式接口
    public interface MainOperator<T> {
        boolean HOLD_LOCK_TAG = false;

        T executeInvokeLogic(boolean result) throws Exception;
    }


}