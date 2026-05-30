package com.fzg.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fzg.mapper.PaperMapper;
import com.fzg.mapper.QuestionMapper;
import com.fzg.model.*;
import com.fzg.service.*;
import com.fzg.vo.AnswerDTO;
import com.fzg.vo.PaperQueryRequest;
import com.fzg.vo.QuestionQueryRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/exam")
public class ExamController {

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
    @Autowired
    private PaperAttemptService paperAttemptService;
    @Autowired
    private PaperAnswerService paperAnswerService;
    @Autowired
    private WrongQuestionService wrongQuestionService;

    /* ------------------- 题库检索 ------------------- */
    @GetMapping("/question/list")
    public Result listQuestions(@RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) String tag,
                                @RequestParam(required = false) Integer difficulty) {
        int safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        int offset = (safePageNum - 1) * safePageSize;

        QuestionQueryRequest request = new QuestionQueryRequest();
        request.setPageNum(safePageNum);
        request.setPageSize(safePageSize);
        request.setKeyword(keyword);
        request.setTag(tag);
        request.setDifficulty(difficulty);

        List<Question> list = questionMapper.queryQuestionList(request, offset);
        Long total = questionMapper.countQuestionList(request);

        Map<String, Object> resp = new HashMap<>();
        resp.put("list", list);
        resp.put("total", total);
        resp.put("pageNum", safePageNum);
        resp.put("pageSize", safePageSize);
        return Result.success(resp);
    }

    /* ------------------- 试卷列表 ------------------- */
    @GetMapping("/paper/list")
    public Result listPapers(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        int safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        int offset = (safePageNum - 1) * safePageSize;

        PaperQueryRequest request = new PaperQueryRequest();
        request.setPageNum(safePageNum);
        request.setPageSize(safePageSize);
        request.setStatus(1);

        List<Paper> papers = paperMapper.selectPaperList(request, offset);
        int total = paperMapper.selectPaperCount(request);

        // 补充题目数量
        List<Map<String, Object>> data = papers.stream().map(p -> {
            long count = paperQuestionService.count(new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId, p.getId()));
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("title", p.getTitle());
            m.put("totalScore", p.getTotalScore());
            m.put("timeLimit", p.getTimeLimit());
            m.put("questionCount", count);
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> resp = new HashMap<>();
        resp.put("total", total);
        resp.put("list", data);
        resp.put("pageNum", safePageNum);
        resp.put("pageSize", safePageSize);
        return Result.success(resp);
    }

    /* ------------------- 试卷详情 ------------------- */
    @GetMapping("/paper/{paperId}")
    public Result paperDetail(@PathVariable Long paperId) {
        Paper paper = paperService.getById(paperId);
        if (paper == null || !Objects.equals(paper.getStatus(), 1)) {
            return Result.fail(404, "试卷不存在");
        }
        List<PaperQuestion> pqList = paperQuestionService.list(new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId, paperId).orderByAsc(PaperQuestion::getSeq));
        if (CollectionUtils.isEmpty(pqList)) {
            return Result.fail(500, "试卷题目为空");
        }
        List<QuestionVO> questions = pqList.stream().map(pq -> {
            Question q = questionService.getById(pq.getQuestionId());
            QuestionVO vo = new QuestionVO();
            vo.setId(q.getId());
            vo.setType(q.getType());
            vo.setContent(q.getContent());
            vo.setOptions(q.getOptions());
            vo.setSeq(pq.getSeq());
            vo.setScore(pq.getScore());
            return vo;
        }).collect(Collectors.toList());
        Map<String, Object> resp = new HashMap<>();
        resp.put("paper", paper);
        resp.put("questions", questions);
        return Result.success(resp);
    }

    /* ------------------- 开始做题 ------------------- */
    @PostMapping("/paper/{paperId}/start")
    public Result startPaper(@PathVariable Long paperId,
                             @RequestParam Long userId) {
        Paper paper = paperService.getById(paperId);
        if (paper == null) return Result.fail(404, "试卷不存在");
        PaperAttempt attempt = new PaperAttempt();
        attempt.setPaperId(paperId);
        attempt.setUserId(userId);
        attempt.setStartTime(new Date());
        attempt.setStatus(0);
        paperAttemptService.save(attempt);
        return Result.success(Collections.singletonMap("attemptId", attempt.getId()));
    }

    /* ------------------- 保存进度 ------------------- */
    @PostMapping("/attempt/{attemptId}/save")
    @Transactional(rollbackFor = Exception.class)
    public Result saveProgress(@PathVariable Long attemptId,
                               @RequestBody List<AnswerDTO> answers) {
        PaperAttempt attempt = paperAttemptService.getById(attemptId);
        if (attempt == null) return Result.fail(404, "答卷不存在");

        for (AnswerDTO dto : answers) {
            PaperAnswer pa = new PaperAnswer();
            pa.setAttemptId(attemptId);
            pa.setQuestionId(dto.getQuestionId());
            pa.setUserAnswer(com.alibaba.fastjson2.JSON.toJSONString(dto.getAnswer()));
            pa.setSpendSeconds(dto.getSpendSeconds());
            // upsert
            LambdaQueryWrapper<PaperAnswer> w = new LambdaQueryWrapper<PaperAnswer>()
                    .eq(PaperAnswer::getAttemptId, attemptId)
                    .eq(PaperAnswer::getQuestionId, dto.getQuestionId());
            PaperAnswer existing = paperAnswerService.getOne(w, false);
            if (existing == null) {
                paperAnswerService.save(pa);
            } else {
                paperAnswerService.update(pa, w);
            }
        }
        return Result.success(true);
    }

    /* ------------------- 提交答卷 ------------------- */
    @PostMapping("/attempt/{attemptId}/submit")
    @Transactional(rollbackFor = Exception.class)
    public Result submitPaper(@PathVariable Long attemptId) {
        PaperAttempt attempt = paperAttemptService.getById(attemptId);
        if (attempt == null) return Result.fail(404, "答卷不存在");

        // 查询试卷题目
        List<PaperQuestion> pqList = paperQuestionService.list(new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId, attempt.getPaperId()));
        Map<Long, PaperQuestion> pqMap = pqList.stream().collect(Collectors.toMap(PaperQuestion::getQuestionId, x -> x));

        List<PaperAnswer> answerList = paperAnswerService.list(new LambdaQueryWrapper<PaperAnswer>().eq(PaperAnswer::getAttemptId, attemptId));
        Map<Long, PaperAnswer> ansMap = answerList.stream().collect(Collectors.toMap(PaperAnswer::getQuestionId, x -> x));

        int totalScore = 0;
        for (PaperQuestion pq : pqList) {
            Question q = questionService.getById(pq.getQuestionId());
            PaperAnswer pa = ansMap.get(q.getId());
            int score = 0;
            boolean correct = false;
            if (pa != null) {
                correct = compareAnswer(q, pa.getUserAnswer());
                if (correct) score = pq.getScore();
                pa.setScore(score);
                paperAnswerService.update(pa, new LambdaQueryWrapper<PaperAnswer>()
                        .eq(PaperAnswer::getAttemptId, attemptId)
                        .eq(PaperAnswer::getQuestionId, pa.getQuestionId()));
            }
            // 错题本钩子：错题入库（累计次数）；曾错过现答对则自动标记为已掌握。
            // 未作答（pa==null）按错题处理，userAnswer 留空。
            try {
                wrongQuestionService.handleAnswer(
                        attempt.getUserId(),
                        attempt.getPaperId(),
                        attemptId,
                        q.getId(),
                        pa == null ? null : pa.getUserAnswer(),
                        correct
                );
            } catch (Exception e) {
                log.warn("错题本记录失败 attemptId={} questionId={} err={}",
                        attemptId, q.getId(), e.getMessage());
            }
            totalScore += score;
        }
        attempt.setTotalScore(totalScore);
        attempt.setSubmitTime(new Date());
        attempt.setStatus(1);
        paperAttemptService.updateById(attempt);
        return Result.success(Collections.singletonMap("totalScore", totalScore));
    }

    /* ------------------- 结果 ------------------- */
    @GetMapping("/attempt/{attemptId}/result")
    public Result attemptResult(@PathVariable Long attemptId) {
        PaperAttempt attempt = paperAttemptService.getById(attemptId);
        if (attempt == null) return Result.fail(404, "答卷不存在");
        List<PaperAnswer> answers = paperAnswerService.list(new LambdaQueryWrapper<PaperAnswer>().eq(PaperAnswer::getAttemptId, attemptId));
        return Result.success(Collections.singletonMap("answers", answers));
    }

    /* ------------------- 工具方法 ------------------- */
    private boolean compareAnswer(Question q, String userAnswerJson) {
        if (userAnswerJson == null) return false;
        try {
            String std = q.getAnswer();
            return std != null && std.trim().equals(userAnswerJson.trim());
        } catch (Exception e) {
            return false;
        }
    }

    /* VO 用于隐藏答案 */
    @Data
    private static class QuestionVO {
        private Long id;
        private Integer type;
        private String content;
        private String options;
        private Integer seq;
        private Integer score;
    }
}
