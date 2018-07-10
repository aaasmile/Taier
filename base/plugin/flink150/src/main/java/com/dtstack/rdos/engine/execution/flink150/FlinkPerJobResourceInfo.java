package com.dtstack.rdos.engine.execution.flink150;

import com.dtstack.rdos.commom.exception.RdosException;
import com.dtstack.rdos.common.util.MathUtil;
import com.dtstack.rdos.common.util.UnitConvertUtil;
import com.dtstack.rdos.engine.execution.base.JobClient;
import com.dtstack.rdos.engine.execution.base.pojo.EngineResourceInfo;

import java.util.Properties;

/**
 * flink yarn 资源相关
 * Date: 2018/07/10
 * Company: www.dtstack.com
 * @author toutian
 */

public class FlinkPerJobResourceInfo extends EngineResourceInfo {

    public final static String CORE_TOTAL_KEY = "cores.total";

    public final static String CORE_USED_KEY = "cores.used";

    public final static String CORE_FREE_KEY = "cores.free";

    public final static String MEMORY_TOTAL_KEY = "memory.total";

    public final static String MEMORY_USED_KEY = "memory.used";

    public final static String MEMORY_FREE_KEY = "memory.free";

    private final static String EXECUTOR_INSTANCES_KEY = "executor.instances";

    private final static String EXECUTOR_MEM_KEY = "executor.memory";

    private final static String EXECUTOR_CORES_KEY = "executor.cores";

    private final static String EXECUTOR_MEM_OVERHEAD_KEY = "yarn.executor.memoryOverhead";

    public final static int DEFAULT_CORES = 1;

    public final static int DEFAULT_INSTANCES = 1;

    public final static int DEFAULT_MEM = 768;

    public final static int DEFAULT_MEM_OVERHEAD = 576;


    @Override
    public boolean judgeSlots(JobClient jobClient) {
        int totalFreeCore = 0;
        int totalFreeMem = 0;

        int totalCore = 0;
        int totalMem = 0;

        for(NodeResourceInfo tmpMap : nodeResourceMap.values()){
            int nodeFreeMem = MathUtil.getIntegerVal(tmpMap.getProp(MEMORY_FREE_KEY));
            int nodeFreeCores = MathUtil.getIntegerVal(tmpMap.getProp(CORE_FREE_KEY));
            int nodeCores = MathUtil.getIntegerVal(tmpMap.getProp(CORE_TOTAL_KEY));
            int nodeMem = MathUtil.getIntegerVal(tmpMap.getProp(MEMORY_TOTAL_KEY));

            totalFreeMem += nodeFreeMem;
            totalFreeCore += nodeFreeCores;
            totalCore += nodeCores;
            totalMem += nodeMem;
        }

        if(totalFreeCore == 0 || totalFreeMem == 0){
            return false;
        }

        Properties properties = jobClient.getConfProperties();
        int instances = DEFAULT_INSTANCES;
        if(properties != null && properties.containsKey(EXECUTOR_INSTANCES_KEY)){
            instances = MathUtil.getIntegerVal(properties.get(EXECUTOR_INSTANCES_KEY));
        }

        return judgeCores(jobClient, instances, totalFreeCore, totalCore)
                && judgeMem(jobClient, instances, totalFreeMem, totalMem);
    }

    private boolean judgeCores(JobClient jobClient, int instances, int freeCore, int totalCore){

        Properties properties = jobClient.getConfProperties();
        int executorCores = DEFAULT_CORES;
        if(properties != null && properties.containsKey(EXECUTOR_CORES_KEY)){
            executorCores = MathUtil.getIntegerVal(properties.get(EXECUTOR_CORES_KEY));
        }

        int needCores = instances * executorCores;
        if(needCores > totalCore){
            throw new RdosException("任务设置的core 大于 集群最大的core");
        }

        return needCores <= freeCore;
    }

    private boolean judgeMem(JobClient jobClient, int instances, int freeMem, int totalMem){
        Properties properties = jobClient.getConfProperties();

        int oneNeedMem = DEFAULT_MEM;
        if(properties != null && properties.containsKey(EXECUTOR_MEM_KEY)){
            String setMemStr = properties.getProperty(EXECUTOR_MEM_KEY);
            oneNeedMem = UnitConvertUtil.convert2MB(setMemStr);
        }

        int executorJvmMem = DEFAULT_MEM_OVERHEAD;
        if(properties != null && properties.containsKey(EXECUTOR_MEM_OVERHEAD_KEY)){
            String setMemStr = properties.getProperty(EXECUTOR_MEM_OVERHEAD_KEY);
            executorJvmMem = UnitConvertUtil.convert2MB(setMemStr);
        }

        oneNeedMem += executorJvmMem;
        int needTotal = instances * oneNeedMem;

        if(needTotal > totalMem){
            throw new RdosException("任务设置的MEM 大于 集群最大的MEM");
        }

        return needTotal <= freeMem;
    }
}
