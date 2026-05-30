package com.fzg.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 错题本：用户作答错误的题目记录
 * <p>
 * 唯一键 (user_id, question_id)：同一用户同一题去重，多次答错累计 wrong_count。
 * 答对后自动置 mastered=1（已掌握），用户也可手动标记或移除。
 */
@Data
@TableName("wrong_question")
public class WrongQuestion {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 题目ID */
    private Long questionId;

    /** 最近一次答错所在试卷ID */
    private Long paperId;

    /** 最近一次答错答卷ID */
    private Long attemptId;

    /** 最近一次的用户错误答案（JSON 字符串） */
    private String lastUserAnswer;

    /** 累计错误次数 */
    private Integer wrongCount;

    /** 是否已掌握 0未掌握 1已掌握 */
    private Integer mastered;

    /** 掌握时间 */
    private Date masteredAt;

    /** 最近一次错误时间 */
    private Date lastWrongAt;

    private Date createdAt;
    private Date updatedAt;
}
