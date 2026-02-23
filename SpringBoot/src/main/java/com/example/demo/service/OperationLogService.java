package com.example.demo.service;

import com.example.demo.entity.OperationLog;
import com.example.demo.entity.User;
import com.example.demo.mapper.OperationLogMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class OperationLogService {

    @Resource
    private OperationLogMapper operationLogMapper;

    /**
     * 记录操作日志
     */
    public void recordOperation(String operationType, String description, User operator, String orderNumber) {
        OperationLog log = new OperationLog();
        log.setOperationType(operationType);
        log.setDescription(description);
        log.setOperatorId(operator.getId());
        log.setOperatorName(operator.getNickName());
        log.setOrderNumber(orderNumber);
        log.setOperateTime(new Date());

        operationLogMapper.insert(log);
    }
}
