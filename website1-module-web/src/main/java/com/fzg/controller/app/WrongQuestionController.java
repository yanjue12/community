package com.fzg.controller.app;

import com.fzg.enums.EnumReturn;
import com.fzg.mapper.WrongQuestionMapper;
import com.fzg.model.Result;
import com.fzg.service.WrongQuestionService;
import com.fzg.vo.WrongQuestionQueryRequest;
import com.fzg.vo.WrongQuestionVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 错题本：用户作答错误的题目自动入库，
 * 提供回顾、按知识点分布、标记掌握、移除等能力，便于查漏补缺。
 */
@Slf4j
@RestController
@RequestMapping("/wrong")
@Tag(name = "错题本", description = "用户错题回顾与针对性复习")
public class WrongQuestionController {

    @Autowired
    private WrongQuestionService wrongQuestionService;

    @Autowired
    private WrongQuestionMapper wrongQuestionMapper;

    /** 错题本分页列表 */
    @PostMapping("/list")
    public Result list(@RequestBody WrongQuestionQueryRequest request) {
        if (request == null || request.getUserId() == null || request.getUserId() <= 0) {
            return Result.fail(EnumReturn.QUERY_PARAM_EMPTY);
        }
        int pageNum = request.getPageNum() == null || request.getPageNum() < 1 ? 1 : request.getPageNum();
        int pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10 : request.getPageSize();
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        int offset = (pageNum - 1) * pageSize;

        List<WrongQuestionVO> list = wrongQuestionMapper.queryWrongList(request, offset);
        Long total = wrongQuestionMapper.countWrongList(request);

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    /** 错题详情（含题干、标准答案、解析、用户最近一次错误答案） */
    @PostMapping("/detail")
    public Result detail(@RequestBody WrongActionRequest req) {
        if (req == null || req.getUserId() == null || req.getId() == null) {
            return Result.fail(EnumReturn.QUERY_PARAM_EMPTY);
        }
        WrongQuestionVO vo = wrongQuestionMapper.queryWrongDetail(req.getUserId(), req.getId());
        if (vo == null) {
            return Result.fail(404, "错题不存在");
        }
        return Result.success(vo);
    }

    /** 用户主动标记某条错题为已掌握 */
    @PostMapping("/master")
    public Result master(@RequestBody WrongActionRequest req) {
        if (req == null || req.getUserId() == null || req.getId() == null) {
            return Result.fail(EnumReturn.QUERY_PARAM_EMPTY);
        }
        return Result.handle(wrongQuestionService.markMastered(req.getUserId(), req.getId()));
    }

    /** 从错题本中移除 */
    @PostMapping("/remove")
    public Result remove(@RequestBody WrongActionRequest req) {
        if (req == null || req.getUserId() == null || req.getId() == null) {
            return Result.fail(EnumReturn.QUERY_PARAM_EMPTY);
        }
        return Result.handle(wrongQuestionService.removeFromBook(req.getUserId(), req.getId()));
    }

    /** 按知识点 tag 聚合错题数（薄弱知识点） */
    @PostMapping("/stats/tag")
    public Result statsByTag(@RequestBody WrongActionRequest req) {
        if (req == null || req.getUserId() == null) {
            return Result.fail(EnumReturn.QUERY_PARAM_EMPTY);
        }
        List<Map<String, Object>> data = wrongQuestionMapper.countByTag(req.getUserId());
        return Result.success(data);
    }

    /** 通用入参：用户ID + 错题记录ID */
    @lombok.Data
    public static class WrongActionRequest {
        private Long userId;
        private Long id;
    }
}
