package com.github.kfcfans.oms.server.web.controller;

import com.github.kfcfans.common.ExecuteType;
import com.github.kfcfans.common.ProcessorType;
import com.github.kfcfans.common.TimeExpressionType;
import com.github.kfcfans.oms.server.common.constans.JobStatus;
import com.github.kfcfans.oms.server.common.utils.CronExpression;
import com.github.kfcfans.oms.server.persistence.PageResult;
import com.github.kfcfans.oms.server.persistence.core.repository.JobInfoRepository;
import com.github.kfcfans.common.response.ResultDTO;
import com.github.kfcfans.oms.server.persistence.core.model.JobInfoDO;
import com.github.kfcfans.oms.server.service.JobService;
import com.github.kfcfans.oms.server.web.request.ModifyJobInfoRequest;
import com.github.kfcfans.oms.server.web.request.QueryJobInfoRequest;
import com.github.kfcfans.oms.server.web.response.JobInfoVO;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 任务信息管理 Controller
 *
 * @author tjq
 * @since 2020/3/30
 */
@Slf4j
@RestController
@RequestMapping("/job")
public class JobController {

    @Resource
    private JobService jobService;
    @Resource
    private JobInfoRepository jobInfoRepository;

    private static final Splitter commaSplitter = Splitter.on(",");
    private static final Joiner commaJoiner = Joiner.on(",").skipNulls();

    @PostMapping("/save")
    public ResultDTO<Void> saveJobInfo(@RequestBody ModifyJobInfoRequest request) throws Exception {

        JobInfoDO jobInfoDO = new JobInfoDO();
        BeanUtils.copyProperties(request, jobInfoDO);

        // 拷贝枚举值
        TimeExpressionType timeExpressionType = TimeExpressionType.valueOf(request.getTimeExpressionType());
        jobInfoDO.setExecuteType(ExecuteType.valueOf(request.getExecuteType()).getV());
        jobInfoDO.setProcessorType(ProcessorType.valueOf(request.getProcessorType()).getV());
        jobInfoDO.setTimeExpressionType(timeExpressionType.getV());
        jobInfoDO.setStatus(request.isEnable() ? JobStatus.ENABLE.getV() : JobStatus.DISABLE.getV());

        if (jobInfoDO.getMaxWorkerCount() == null) {
            jobInfoDO.setMaxInstanceNum(0);
        }

        // 转化报警用户列表
        if (!CollectionUtils.isEmpty(request.getNotifyUserIds())) {
            jobInfoDO.setNotifyUserIds(commaJoiner.join(request.getNotifyUserIds()));
        }

        // 计算下次调度时间
        Date now = new Date();
        if (timeExpressionType == TimeExpressionType.CRON) {
            CronExpression cronExpression = new CronExpression(request.getTimeExpression());
            Date nextValidTime = cronExpression.getNextValidTimeAfter(now);
            jobInfoDO.setNextTriggerTime(nextValidTime.getTime());
        }

        if (request.getId() == null) {
            jobInfoDO.setGmtCreate(now);
        }
        jobInfoDO.setGmtModified(now);
        jobInfoRepository.saveAndFlush(jobInfoDO);

        return ResultDTO.success(null);
    }

    @GetMapping("/disable")
    public ResultDTO<Void> disableJob(String jobId) throws Exception {
        jobService.disableJob(Long.valueOf(jobId));
        return ResultDTO.success(null);
    }

    @GetMapping("/delete")
    public ResultDTO<Void> deleteJob(String jobId) throws Exception {
        jobService.deleteJob(Long.valueOf(jobId));
        return ResultDTO.success(null);
    }

    @GetMapping("/run")
    public ResultDTO<Long> runImmediately(String jobId) {
        return ResultDTO.success(jobService.runJob(Long.valueOf(jobId), null));
    }

    @PostMapping("/list")
    public ResultDTO<PageResult<JobInfoVO>> listJobs(@RequestBody QueryJobInfoRequest request) {

        Sort sort = Sort.by(Sort.Direction.DESC, "gmtCreate");
        PageRequest pageRequest = PageRequest.of(request.getIndex(), request.getPageSize(), sort);
        Page<JobInfoDO> jobInfoPage;

        // 无查询条件，查询全部
        if (request.getJobId() == null && StringUtils.isEmpty(request.getKeyword())) {
            jobInfoPage = jobInfoRepository.findByAppIdAndStatusNot(request.getAppId(), pageRequest, JobStatus.DELETED.getV());
            return ResultDTO.success(convertPage(jobInfoPage));
        }

        // 有 jobId，直接精确查询
        if (request.getJobId() != null) {

            Optional<JobInfoDO> jobInfoOpt = jobInfoRepository.findById(request.getJobId());
            PageResult<JobInfoVO> result = new PageResult<>();
            result.setIndex(0);
            result.setPageSize(request.getPageSize());

            if (jobInfoOpt.isPresent()) {
                result.setTotalItems(1);
                result.setTotalPages(1);
                result.setData(Lists.newArrayList(convert(jobInfoOpt.get())));
            }else {
                result.setTotalPages(0);
                result.setTotalItems(0);
                result.setData(Lists.newLinkedList());
            }

            return ResultDTO.success(result);
        }

        // 模糊查询
        String condition = "%" + request.getKeyword() + "%";
        jobInfoPage = jobInfoRepository.findByAppIdAndJobNameLikeAndStatusNot(request.getAppId(), condition, JobStatus.DELETED.getV(), pageRequest);
        return ResultDTO.success(convertPage(jobInfoPage));
    }




    private static PageResult<JobInfoVO> convertPage(Page<JobInfoDO> jobInfoPage) {
        List<JobInfoVO> jobInfoVOList = jobInfoPage.getContent().stream().map(JobController::convert).collect(Collectors.toList());

        PageResult<JobInfoVO> pageResult = new PageResult<>(jobInfoPage);
        pageResult.setData(jobInfoVOList);
        return pageResult;
    }

    private static JobInfoVO convert(JobInfoDO jobInfoDO) {
        JobInfoVO jobInfoVO = new JobInfoVO();
        BeanUtils.copyProperties(jobInfoDO, jobInfoVO);

        TimeExpressionType timeExpressionType = TimeExpressionType.of(jobInfoDO.getTimeExpressionType());
        ExecuteType executeType = ExecuteType.of(jobInfoDO.getExecuteType());
        ProcessorType processorType = ProcessorType.of(jobInfoDO.getProcessorType());

        jobInfoVO.setTimeExpressionType(timeExpressionType.name());
        jobInfoVO.setExecuteType(executeType.name());
        jobInfoVO.setProcessorType(processorType.name());
        jobInfoVO.setEnable(jobInfoDO.getStatus() == JobStatus.ENABLE.getV());

        if (!StringUtils.isEmpty(jobInfoDO.getNotifyUserIds())) {
            jobInfoVO.setNotifyUserIds(commaSplitter.splitToList(jobInfoDO.getNotifyUserIds()));
        }else {
            jobInfoVO.setNotifyUserIds(Lists.newLinkedList());
        }

        return jobInfoVO;
    }


}