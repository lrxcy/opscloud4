package com.baiyi.opscloud.facade.sys.impl;

import com.baiyi.opscloud.common.builder.SimpleDict;
import com.baiyi.opscloud.common.builder.SimpleDictBuilder;
import com.baiyi.opscloud.common.util.SessionUtil;
import com.baiyi.opscloud.common.util.TemplateUtil;
import com.baiyi.opscloud.domain.generator.opscloud.Document;
import com.baiyi.opscloud.domain.param.sys.DocumentParam;
import com.baiyi.opscloud.domain.vo.sys.DocumentVO;
import com.baiyi.opscloud.facade.sys.DocumentFacade;
import com.baiyi.opscloud.service.sys.DocumentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @Author baiyi
 * @Date 2021/6/16 11:29 上午
 * @Version 1.0
 */
@Service
public class DocumentFacadeImpl implements DocumentFacade {

    @Resource
    private DocumentService documentService;

    @Override
    public DocumentVO.Doc previewDocument(DocumentParam.DocumentQuery query) {
        Document doc = documentService.getByKey(query.getDocumentKey());
        render(doc, query);

        return DocumentVO.Doc.builder()
                .content(doc.getContent())
                .dict(query.getDict())
                .build();
    }

    private void render(Document doc, DocumentParam.DocumentQuery query) {
        render(doc, query.getDict()); // 反向注入

        SimpleDict simpleDict = SimpleDictBuilder.newBuilder()
                .paramEntry("username", SessionUtil.getUsername())
                .build();
        render(doc, simpleDict.getDict());
    }

    private void render(Document doc, Map<String, String> dict) {
        if (dict != null) {
            String content = TemplateUtil.render(doc.getContent(), dict);
            doc.setContent(content);
        }
    }
}
