package com.gce.lms.common.metrics.cloudwatch;

import io.micrometer.core.lang.Nullable;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.util.Optional;
import java.util.function.ToLongFunction;

/**
 * Class for providing details for jvm memory pool.
 * Talks about memory usage, memory available and
 * memory maximum available for use.
 * This data can be put in cloudwatch to create a metrics
 * over which alarms can be set.
 */
class JvmMemory {
    private JvmMemory() {
    }

    static Optional<MemoryPoolMXBean> getOldGen() {
        return ManagementFactory.getPlatformMXBeans(MemoryPoolMXBean.class)
            .stream().filter(JvmMemory::isHeap).filter((mem) -> {
                return isOldGenPool(mem.getName());
            }).findAny();
    }

    static boolean isConcurrentPhase(String cause) {
        return "No GC".equals(cause);
    }

    static boolean isYoungGenPool(String name) {
        return name.endsWith("Eden Space");
    }

    static boolean isOldGenPool(String name) {
        return name.endsWith("Old Gen") || name.endsWith("Tenured Gen");
    }

    private static boolean isHeap(MemoryPoolMXBean memoryPoolBean) {
        return MemoryType.HEAP.equals(memoryPoolBean.getType());
    }

    static double getUsageValue(MemoryPoolMXBean memoryPoolMxBean, ToLongFunction<MemoryUsage> getter) {
        MemoryUsage usage = getUsage(memoryPoolMxBean);
        return usage == null ? 0.0D / 0.0 : (double)getter.applyAsLong(usage);
    }

    @Nullable
    private static MemoryUsage getUsage(MemoryPoolMXBean memoryPoolMxBean) {
        try {
            return memoryPoolMxBean.getUsage();
        } catch (InternalError var2) {
            return null;
        }
    }
}
