package com.baiyi.opscloud.facade.datasource.impl;

import com.baiyi.opscloud.ansible.args.PlaybookArgs;
import com.baiyi.opscloud.ansible.builder.AnsiblePlaybookArgsBuilder;
import com.baiyi.opscloud.ansible.handler.AnsibleHandler;
import com.baiyi.opscloud.ansible.model.AnsibleExecuteResult;
import com.baiyi.opscloud.ansible.util.AnsibleUtil;
import com.baiyi.opscloud.common.datasource.AnsibleDsInstanceConfig;
import com.baiyi.opscloud.common.datasource.config.DsAnsibleConfig;
import com.baiyi.opscloud.common.util.BeanCopierUtil;
import com.baiyi.opscloud.common.util.IOUtil;
import com.baiyi.opscloud.common.util.TimeUtil;
import com.baiyi.opscloud.datasource.factory.DsConfigFactory;
import com.baiyi.opscloud.datasource.model.DsInstanceContext;
import com.baiyi.opscloud.datasource.provider.base.common.SimpleDsInstanceProvider;
import com.baiyi.opscloud.datasource.util.SystemEnvUtil;
import com.baiyi.opscloud.domain.DataTable;
import com.baiyi.opscloud.domain.generator.opscloud.DatasourceInstanceAssetSubscription;
import com.baiyi.opscloud.domain.param.datasource.DsAssetSubscriptionParam;
import com.baiyi.opscloud.domain.vo.datasource.DsAssetSubscriptionVO;
import com.baiyi.opscloud.facade.datasource.DsInstanceAssetSubscriptionFacade;
import com.baiyi.opscloud.packer.datasource.DsAssetSubscriptionPacker;
import com.baiyi.opscloud.service.datasource.DsInstanceAssetSubscriptionService;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * @Author baiyi
 * @Date 2021/8/27 4:05 下午
 * @Version 1.0
 */
@Slf4j
@Service
public class DsInstanceAssetSubscriptionFacadeImpl extends SimpleDsInstanceProvider implements DsInstanceAssetSubscriptionFacade {

    @Resource
    private DsAssetSubscriptionPacker dsAssetSubscriptionPacker;

    @Resource
    private DsInstanceAssetSubscriptionService dsInstanceAssetSubscriptionService;

    @Resource
    private DsConfigFactory dsConfigFactory;

    @Override
    public DataTable<DsAssetSubscriptionVO.AssetSubscription> queryAssetSubscriptionPage(DsAssetSubscriptionParam.AssetSubscriptionPageQuery pageQuery) {
        DataTable<DatasourceInstanceAssetSubscription> table = dsInstanceAssetSubscriptionService.queryPageByParam(pageQuery);
        return new DataTable<>(
                table.getData().stream().map(e -> dsAssetSubscriptionPacker.wrapToVO(e, pageQuery)).collect(Collectors.toList()),
                table.getTotalNum());
    }

    @Override
    public void updateAssetSubscription(DsAssetSubscriptionVO.AssetSubscription assetSubscription) {
        DatasourceInstanceAssetSubscription pre = BeanCopierUtil.copyProperties(assetSubscription, DatasourceInstanceAssetSubscription.class);
        DatasourceInstanceAssetSubscription datasourceInstanceAssetSubscription = dsInstanceAssetSubscriptionService.getById(assetSubscription.getId());
        datasourceInstanceAssetSubscription.setPlaybook(pre.getPlaybook());
        datasourceInstanceAssetSubscription.setVars(pre.getVars());
        datasourceInstanceAssetSubscription.setComment(pre.getComment());
        dsInstanceAssetSubscriptionService.update(datasourceInstanceAssetSubscription);
        saveSubscriptionPlaybookFile(datasourceInstanceAssetSubscription);
    }

    /**
     * 发布
     *
     * @param id
     */
    @Override
    public void publishAssetSubscriptionById(int id) {
        DatasourceInstanceAssetSubscription datasourceInstanceAssetSubscription = dsInstanceAssetSubscriptionService.getById(id);
        publishAssetSubscription(datasourceInstanceAssetSubscription);
    }

    @Override
    public void publishAssetSubscription(DatasourceInstanceAssetSubscription datasourceInstanceAssetSubscription) {
        DsInstanceContext instanceContext = buildDsInstanceContext(datasourceInstanceAssetSubscription.getInstanceUuid());
        DsAnsibleConfig.Ansible ansible = dsConfigFactory.build(instanceContext.getDsConfig(), AnsibleDsInstanceConfig.class).getAnsible();
        PlaybookArgs args = PlaybookArgs.builder()
                .extraVars(AnsibleUtil.toVars(datasourceInstanceAssetSubscription.getVars()).getVars())
                .keyFile(SystemEnvUtil.renderEnvHome(ansible.getPrivateKey()))
                .playbook(toSubscriptionPlaybookFile(ansible, datasourceInstanceAssetSubscription))
                .inventory(SystemEnvUtil.renderEnvHome(ansible.getInventoryHost()))
                .build();
        CommandLine commandLine = AnsiblePlaybookArgsBuilder.build(ansible, args);
        AnsibleExecuteResult er = AnsibleHandler.execute(commandLine, TimeUtil.minuteTime * 2);
        try {
            datasourceInstanceAssetSubscription.setLastSubscriptionLog(er.getOutput().toString("utf8"));
            datasourceInstanceAssetSubscription.setLastSubscriptionTime(new Date());
            dsInstanceAssetSubscriptionService.update(datasourceInstanceAssetSubscription);
        } catch (UnsupportedEncodingException e) {
            log.error("发布订阅任务失败！id = {}", datasourceInstanceAssetSubscription.getId());
        }
    }

    @Override
    public void addAssetSubscription(DsAssetSubscriptionVO.AssetSubscription assetSubscription) {
        DatasourceInstanceAssetSubscription pre = BeanCopierUtil.copyProperties(assetSubscription, DatasourceInstanceAssetSubscription.class);
        dsInstanceAssetSubscriptionService.add(pre);
        saveSubscriptionPlaybookFile(pre);
    }

    private void saveSubscriptionPlaybookFile(DatasourceInstanceAssetSubscription subscription) {
        if (StringUtils.isEmpty(subscription.getPlaybook())) return;
        String file = toSubscriptionPlaybookFile(subscription);
        IOUtil.writeFile(subscription.getPlaybook(), file);
    }

    /**
     * 转换订阅配置剧本文件
     *
     * @param subscription
     * @return
     */
    public String toSubscriptionPlaybookFile(DatasourceInstanceAssetSubscription subscription) {
        DsInstanceContext instanceContext = buildDsInstanceContext(subscription.getInstanceUuid());
        DsAnsibleConfig.Ansible ansible = dsConfigFactory.build(instanceContext.getDsConfig(), AnsibleDsInstanceConfig.class).getAnsible();
        return toSubscriptionPlaybookFile(ansible, subscription);
    }

    private String toSubscriptionPlaybookFile(DsAnsibleConfig.Ansible ansible, DatasourceInstanceAssetSubscription subscription) {
        String fileName = Joiner.on("_").join(subscription.getInstanceUuid(), subscription.getDatasourceInstanceAssetId(), subscription.getId()) + ".yml";
        String path = Joiner.on("/").join(ansible.getData(), "subscription", fileName);
        return SystemEnvUtil.renderEnvHome(path);
    }

    @Override
    public void deleteAssetSubscriptionById(int id) {
        dsInstanceAssetSubscriptionService.deleteById(id);
    }

}
