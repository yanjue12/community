package com.fzg.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fzg.mapper.PaperMapper;
import com.fzg.mapper.QuestionMapper;
import com.fzg.model.*;
import com.fzg.service.PaperQuestionService;
import com.fzg.service.PaperService;
import com.fzg.service.QuestionService;
import com.fzg.vo.PaperQueryRequest;
import com.fzg.vo.QuestionQueryRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/exam")
@SaCheckLogin
@SaCheckRole(value = {"admin", "auditAdmin"}, mode = SaMode.OR)
@Tag(name = "管理端题库试卷管理", description = "题目与试卷管理接口")
public class AdminExamController {

    @Autowired
    private QuestionService questionService;
    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private PaperService paperService;
    @Autowired
    private PaperMapper paperMapper;

    @Autowired
    private PaperQuestionService paperQuestionService;

    @PostMapping("/question/list")
    @Operation(summary = "题目列表", description = "支持关键词、题型、难度、标签多条件分页查询")
    public Result listQuestions(@RequestBody(required = false) QuestionQueryRequest request) {
        if (request == null) {
            request = new QuestionQueryRequest();
        }
        int pageNum = request.getPageNum() == null || request.getPageNum() < 1 ? 1 : request.getPageNum();
        int pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10 : request.getPageSize();

        int offset = (pageNum - 1) * pageSize;
        List<Question> list = questionMapper.queryQuestionList(request, offset);
        Long total = questionMapper.countQuestionList(request);
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        return Result.success(data);
    }

    @GetMapping("/question/{id}")
    @Operation(summary = "题目详情")
    public Result getQuestion(@PathVariable Long id) {
        Question question = questionService.getById(id);
        if (question == null) {
            return Result.fail(404, "题目不存在");
        }
        return Result.success(question);
    }

    @PostMapping("/question/create")
    @SaCheckRole("admin")
    @Operation(summary = "新增题目")
    public Result createQuestion(@RequestBody Question question) {
        if (question == null || question.getType() == null || !notBlank(question.getContent()) || !notBlank(question.getAnswer())) {
            return Result.fail(400, "题目类型、题干、标准答案不能为空");
        }
        if (question.getDifficulty() == null) {
            question.setDifficulty(2);
        }
        String normalizedAnswer = normalizeAnswer(question.getAnswer(), question.getType());
        if (!notBlank(normalizedAnswer)) {
            return Result.fail(400, "标准答案格式不合法");
        }
        String normalizedOptions = normalizeOptions(question.getOptions(), question.getType());
        if ((question.getType() == 1 || question.getType() == 2) && !notBlank(normalizedOptions)) {
            return Result.fail(400, "选择题选项格式不合法");
        }
        String normalizedTags = normalizeTags(question.getTags());

        question.setAnswer(normalizedAnswer);
        question.setOptions(normalizedOptions);
        question.setTags(normalizedTags);

        question.setCreatedAt(new Date());
        question.setUpdatedAt(new Date());
        return Result.handle(questionService.save(question));
    }

    @PutMapping("/question/update")
    @SaCheckRole("admin")
    @Operation(summary = "修改题目")
    public Result updateQuestion(@RequestBody Question question) {
        if (question == null || question.getId() == null) {
            return Result.fail(400, "题目ID不能为空");
        }
        Question db = questionService.getById(question.getId());
        if (db == null) {
            return Result.fail(404, "题目不存在");
        }

        Integer effectiveType = question.getType() == null ? db.getType() : question.getType();
        if (question.getAnswer() != null) {
            String normalizedAnswer = normalizeAnswer(question.getAnswer(), effectiveType);
            if (!notBlank(normalizedAnswer)) {
                return Result.fail(400, "标准答案格式不合法");
            }
            question.setAnswer(normalizedAnswer);
        }
        if (question.getOptions() != null) {
            String normalizedOptions = normalizeOptions(question.getOptions(), effectiveType);
            if ((effectiveType == 1 || effectiveType == 2) && !notBlank(normalizedOptions)) {
                return Result.fail(400, "选择题选项格式不合法");
            }
            question.setOptions(normalizedOptions);
        }
        if (question.getTags() != null) {
            question.setTags(normalizeTags(question.getTags()));
        }

        question.setUpdatedAt(new Date());
        return Result.handle(questionService.updateById(question));
    }

    @DeleteMapping("/question/{id}")
    @SaCheckRole("admin")
    @Operation(summary = "删除题目")
    public Result deleteQuestion(@PathVariable Long id) {
        Long usedCount = paperQuestionService.count(new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getQuestionId, id));
        if (usedCount != null && usedCount > 0) {
            return Result.fail(400, "该题目已被试卷引用，无法删除");
        }
        return Result.handle(questionService.removeById(id));
    }

    @PostMapping("/paper/list")
    @Operation(summary = "试卷列表", description = "支持标题关键字、状态多条件分页查询")
    public Result listPapers(@RequestBody(required = false) PaperQueryRequest request) {
        if (request == null) {
            request = new PaperQueryRequest();
        }
        int pageNum = request.getPageNum() == null || request.getPageNum() < 1 ? 1 : request.getPageNum();
        int pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10 : request.getPageSize();
        int offset = (pageNum -1)*pageSize;

        List<Paper> papers = paperMapper.selectPaperList(request,offset);
        int total = paperMapper.selectPaperCount(request);
        List<Map<String, Object>> list = papers.stream().map(p -> {
            long questionCount = paperQuestionService.count(new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId, p.getId()));
            Map<String, Object> item = new HashMap<>();
            item.put("id", p.getId());
            item.put("title", p.getTitle());
            item.put("status", p.getStatus());
            item.put("totalScore", p.getTotalScore());
            item.put("timeLimit", p.getTimeLimit());
            item.put("createdAt", p.getCreatedAt());
            item.put("updatedAt", p.getUpdatedAt());
            item.put("questionCount", questionCount);
            return item;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total",total);
        return Result.success(data);
    }

    @GetMapping("/paper/{paperId}")
    @Operation(summary = "试卷详情", description = "返回试卷基础信息和选题列表")
    public Result getPaperDetail(@PathVariable Long paperId) {
        Paper paper = paperService.getById(paperId);
        if (paper == null) {
            return Result.fail(404, "试卷不存在");
        }
        List<PaperQuestion> pqList = paperQuestionService.list(
                new LambdaQueryWrapper<PaperQuestion>()
                        .eq(PaperQuestion::getPaperId, paperId)
                        .orderByAsc(PaperQuestion::getSeq)
        );

        List<Long> questionIds = pqList.stream().map(PaperQuestion::getQuestionId).collect(Collectors.toList());
        Map<Long, Question> questionMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(questionIds)) {
            List<Question> questions = questionService.listByIds(questionIds);
            questionMap = questions.stream().collect(Collectors.toMap(Question::getId, q -> q, (a, b) -> a));
        }

        List<Map<String, Object>> questions = new ArrayList<>();
        for (PaperQuestion pq : pqList) {
            Map<String, Object> item = new HashMap<>();
            item.put("questionId", pq.getQuestionId());
            item.put("seq", pq.getSeq());
            item.put("score", pq.getScore());
            Question q = questionMap.get(pq.getQuestionId());
            if (q != null) {
                item.put("type", q.getType());
                item.put("content", q.getContent());
                item.put("difficulty", q.getDifficulty());
                item.put("tags", q.getTags());
                item.put("options", q.getOptions());
            }
            questions.add(item);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("paper", paper);
        data.put("questions", questions);
        return Result.success(data);
    }

    @PostMapping("/paper/create")
    @SaCheckRole("admin")
    @Transactional(rollbackFor = Exception.class)
    @Operation(summary = "新增试卷", description = "可同时提交题目列表组成试卷")
    public Result createPaper(@RequestBody PaperSaveRequest request) {
        if (request == null || !notBlank(request.getTitle())) {
            return Result.fail(400, "试卷标题不能为空");
        }

        Paper paper = new Paper();
        paper.setTitle(request.getTitle().trim());
        paper.setTimeLimit(request.getTimeLimit());
        paper.setStatus(request.getStatus() == null ? 0 : request.getStatus());
        paper.setTotalScore(0);
        paper.setCreatedAt(new Date());
        paper.setUpdatedAt(new Date());
        boolean ok = paperService.save(paper);
        if (!ok) {
            return Result.fail(500, "创建试卷失败");
        }

        int totalScore = savePaperQuestions(paper.getId(), request.getQuestions());
        Paper update = new Paper();
        update.setId(paper.getId());
        update.setTotalScore(totalScore);
        update.setUpdatedAt(new Date());
        paperService.updateById(update);

        return Result.success(Collections.singletonMap("paperId", paper.getId()));
    }

    @PutMapping("/paper/update")
    @SaCheckRole("admin")
    @Transactional(rollbackFor = Exception.class)
    @Operation(summary = "修改试卷", description = "可修改基础信息并重置选题")
    public Result updatePaper(@RequestBody PaperSaveRequest request) {
        if (request == null || request.getId() == null) {
            return Result.fail(400, "试卷ID不能为空");
        }
        Paper db = paperService.getById(request.getId());
        if (db == null) {
            return Result.fail(404, "试卷不存在");
        }

        Paper update = new Paper();
        update.setId(request.getId());
        if (notBlank(request.getTitle())) {
            update.setTitle(request.getTitle().trim());
        }
        update.setTimeLimit(request.getTimeLimit());
        if (request.getStatus() != null) {
            update.setStatus(request.getStatus());
        }

        Integer totalScore = db.getTotalScore();
        if (request.getQuestions() != null) {
            totalScore = savePaperQuestions(request.getId(), request.getQuestions());
        }
        update.setTotalScore(totalScore == null ? 0 : totalScore);
        update.setUpdatedAt(new Date());
        return Result.handle(paperService.updateById(update));
    }

    @PutMapping("/paper/{paperId}/questions")
    @SaCheckRole("admin")
    @Transactional(rollbackFor = Exception.class)
    @Operation(summary = "重置试卷题目", description = "从题库选择题目组成试卷")
    public Result resetPaperQuestions(@PathVariable Long paperId,
                                      @RequestBody List<PaperQuestionItem> questions) {
        Paper paper = paperService.getById(paperId);
        if (paper == null) {
            return Result.fail(404, "试卷不存在");
        }
        int totalScore = savePaperQuestions(paperId, questions);
        Paper update = new Paper();
        update.setId(paperId);
        update.setTotalScore(totalScore);
        update.setUpdatedAt(new Date());
        paperService.updateById(update);
        return Result.success(true);
    }

    @DeleteMapping("/paper/{paperId}")
    @SaCheckRole("admin")
    @Transactional(rollbackFor = Exception.class)
    @Operation(summary = "删除试卷")
    public Result deletePaper(@PathVariable Long paperId) {
        paperQuestionService.remove(new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId, paperId));
        return Result.handle(paperService.removeById(paperId));
    }

    private int savePaperQuestions(Long paperId, List<PaperQuestionItem> questions) {
        paperQuestionService.remove(new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId, paperId));

        if (CollectionUtils.isEmpty(questions)) {
            return 0;
        }

        Set<Long> questionIdSet = questions.stream()
                .map(PaperQuestionItem::getQuestionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (CollectionUtils.isEmpty(questionIdSet)) {
            return 0;
        }

        List<Question> dbQuestions = questionService.listByIds(questionIdSet);
        Set<Long> existIds = dbQuestions.stream().map(Question::getId).collect(Collectors.toSet());

        int total = 0;
        int seq = 1;
        for (PaperQuestionItem item : questions) {
            if (item.getQuestionId() == null || !existIds.contains(item.getQuestionId())) {
                continue;
            }
            PaperQuestion pq = new PaperQuestion();
            pq.setPaperId(paperId);
            pq.setQuestionId(item.getQuestionId());
            pq.setSeq(item.getSeq() == null ? seq : item.getSeq());
            int score = item.getScore() == null || item.getScore() <= 0 ? 5 : item.getScore();
            pq.setScore(score);
            paperQuestionService.save(pq);
            total += score;
            seq++;
        }
        return total;
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String normalizeAnswer(String answer, Integer type) {
        if (!notBlank(answer) || type == null) {
            return null;
        }
        String raw = answer.trim();

        if (type == 1) {
            if (isValidJson(raw)) {
                Object parsed = JSON.parse(raw);
                if (parsed instanceof JSONArray) {
                    return raw;
                }
                if (parsed instanceof String) {
                    return JSON.toJSONString(Collections.singletonList(parsed.toString()));
                }
            }
            return JSON.toJSONString(Collections.singletonList(raw.replace("\"", "")));
        }

        if (type == 2) {
            if (isValidJson(raw)) {
                Object parsed = JSON.parse(raw);
                if (parsed instanceof JSONArray) {
                    return raw;
                }
            }
            List<String> arr = Arrays.stream(raw.split("[,，]"))
                    .map(String::trim)
                    .filter(this::notBlank)
                    .map(s -> s.replace("\"", ""))
                    .collect(Collectors.toList());
            return arr.isEmpty() ? null : JSON.toJSONString(arr);
        }

        if (isValidJson(raw)) {
            return raw;
        }
        return JSON.toJSONString(raw);
    }

    private String normalizeOptions(String options, Integer type) {
        if (type != null && type == 3) {
            return null;
        }
        if (!notBlank(options)) {
            return null;
        }
        String raw = options.trim();
        if (!isValidJson(raw)) {
            return null;
        }
        Object parsed = JSON.parse(raw);
        return parsed instanceof JSONArray ? raw : null;
    }

    private String normalizeTags(String tags) {
        if (!notBlank(tags)) {
            return null;
        }
        String raw = tags.trim();
        if (isValidJson(raw)) {
            return raw;
        }
        List<String> arr = Arrays.stream(raw.split("[,，]"))
                .map(String::trim)
                .filter(this::notBlank)
                .collect(Collectors.toList());
        return arr.isEmpty() ? null : JSON.toJSONString(arr);
    }

    private boolean isValidJson(String raw) {
        try {
            JSON.parse(raw);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    @Data
    public static class PaperSaveRequest {
        private Long id;
        private String title;
        private Integer timeLimit;
        private Integer status;
        private List<PaperQuestionItem> questions;
    }

    @Data
    public static class PaperQuestionItem {
        private Long questionId;
        private Integer seq;
        private Integer score;
    }
}
