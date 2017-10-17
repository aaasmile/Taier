package com.dtstack.rdos.engine.execution.base;

import com.dtstack.rdos.engine.execution.base.enumeration.RdosTaskStatus;
import com.dtstack.rdos.engine.execution.base.pojo.JobResult;
import com.dtstack.rdos.engine.execution.base.pojo.ParamAction;
import java.io.IOException;
import java.util.Properties;

/**
 * Reason:
 * Date: 2017/2/20
 * Company: www.dtstack.com
 *
 * @ahthor xuchao
 */

public interface IClient {

    /**
     * FIXME 根据zk做初始化的时候的操作
     */
    void init(Properties prop) throws Exception;

    /**
     * 提交的时候先判断下计算资源是否足够,
     * 只有sql方式才能判断,jar方式计算资源在jar包里面指定,无法获取
     * @param jobClient
     * @return
     */
    JobResult submitJob(JobClient jobClient);

    JobResult cancelJob(ParamAction jobId);

    RdosTaskStatus getJobStatus(String jobId) throws IOException;

	String getJobMaster();
	
	String getMessageByHttp(String path);
	
    }
