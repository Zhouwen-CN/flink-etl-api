package com.etl.api.ai.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.etl.api.ai.domain.convert.AiBotConvert;
import com.etl.api.ai.domain.entity.AiConversation;
import com.etl.api.ai.domain.entity.AiConversationMessage;
import com.etl.api.ai.domain.form.AiChatForm;
import com.etl.api.ai.domain.form.AiConversationCreateForm;
import com.etl.api.ai.domain.form.AiConversationRenameForm;
import com.etl.api.ai.domain.vo.AiConversationVO;
import com.etl.api.ai.service.AiBotService;
import com.etl.api.ai.service.AiChatService;
import com.etl.api.ai.service.AiConversationMessageService;
import com.etl.api.ai.service.AiConversationService;
import com.etl.api.sys.domain.vo.DictionaryVO;
import com.etl.api.sys.domain.vo.ResponseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Tag(name = "AI聊天 控制器")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/chat")
public class AiChatController {

    private final AiBotService botService;
    private final AiChatService chatService;
    private final AiConversationService conversationService;
    private final AiConversationMessageService conversationMessageService;

    @SaCheckPermission("chat.select")
    @Operation(summary = "Bot选择器")
    @GetMapping("/bot/selector")
    public ResponseVO<List<DictionaryVO>> listBotSelector() {
        List<DictionaryVO> list = botService.queryChain()
                .list()
                .stream()
                .map(AiBotConvert.INSTANCE::convert)
                .toList();
        return ResponseVO.ok(list);
    }

    @SaCheckPermission("chat.select")
    @Operation(summary = "查询会话列表")
    @GetMapping("/conversation")
    public ResponseVO<List<AiConversationVO>> listConversation() {
        List<AiConversationVO> vos = conversationService.queryChain()
                .eq(AiConversation::getUserId, StpUtil.getLoginIdAsLong())
                .orderBy(AiConversation::getCreateTime, false)
                .listAs(AiConversationVO.class);
        return ResponseVO.ok(vos);
    }

    @SaCheckPermission("chat.select")
    @Operation(summary = "查询会话消息列表")
    @GetMapping("/conversation/message")
    public ResponseVO<List<String>> listConversationMessage(
            @RequestParam("conversationId") Long conversationId
    ) {
        List<String> messages = conversationMessageService.queryChain()
                .select(AiConversationMessage::getMessage)
                .eq(AiConversationMessage::getConversationId, conversationId)
                .orderBy(AiConversationMessage::getId, true)
                .listAs(String.class);
        return ResponseVO.ok(messages);
    }

    @SaCheckPermission("chat.insert")
    @Operation(summary = "新增会话")
    @PostMapping("/conversation")
    public ResponseVO<AiConversationVO> createConversation(@RequestBody @Validated AiConversationCreateForm form) {
        AiConversation conversation = conversationService.createConversation(form.getBotId(), form.getMessage());
        AiConversationVO vo = new AiConversationVO();
        vo.setId(conversation.getId());
        vo.setTitle(conversation.getTitle());
        return ResponseVO.ok(vo);
    }

    @SaCheckPermission("chat.delete")
    @Operation(summary = "删除会话（含会话消息）")
    @DeleteMapping("/conversation/{id}")
    public ResponseVO<Void> deleteConversation(@PathVariable @Parameter(description = "会话ID") Long id) {
        conversationService.deleteConversation(id);
        return ResponseVO.ok();
    }

    @SaCheckPermission("chat.update")
    @Operation(summary = "会话重命名")
    @PatchMapping("/conversation")
    public ResponseVO<Void> renameConversation(@RequestBody @Validated AiConversationRenameForm form) {
        conversationService.renameConversation(form.getId(), form.getTitle());
        return ResponseVO.ok();
    }

    @SaCheckPermission("chat.insert")
    @Operation(summary = "聊天（SSE 流式）")
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody @Validated AiChatForm form) {
        return chatService.streamChat(form);
    }
}
