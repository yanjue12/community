package com.fzg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.enums.EnumReturn;
import com.fzg.mapper.SubtitlesMapper;
import com.fzg.model.Result;
import com.fzg.model.Solutions;
import com.fzg.model.Subtitles;
import com.fzg.service.SolutionsService;
import com.fzg.mapper.SolutionsMapper;
import com.fzg.vo.SolutionsVO;
import com.fzg.vo.SubtitleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
* @author yanju
* @description 针对表【solutions(solution表)】的数据库操作Service实现
* @createDate 2025-07-09 17:09:29
*/
@Service
@Slf4j
public class SolutionsServiceImpl extends ServiceImpl<SolutionsMapper, Solutions>
    implements SolutionsService {


    @Resource
    private SubtitlesMapper subtitlesMapper;

    /**
     * 列出所有解决方案(解决方案表 子标题表)
     */
    @Override
    public List<SolutionsVO> solutionsList() {
        //查询状态为1的解决方案
        LambdaQueryWrapper<Solutions> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Solutions::getStates, 1);
        List<Solutions> solutionsList = this.list(queryWrapper);

        List<SolutionsVO> solutionsVOList = new ArrayList<>();

        //查询状态为1的解决方案的子标题
        for (Solutions solutions : solutionsList) {
            SolutionsVO solutionsVO = new SolutionsVO();

            solutionsVO.setUrl(solutions.getImageUrl());
            solutionsVO.setTitle(solutions.getTitle());

            LambdaQueryWrapper<Subtitles> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1
                    .eq(Subtitles::getSolutionId, solutions.getId())
                    .eq(Subtitles::getStates, 1);
            List<Subtitles> subtitlesList = subtitlesMapper.selectList(queryWrapper1);


            //封装子标题到解决方案VO中
            List<SubtitleVO> subtitlesVOList = new ArrayList<>();
            for (Subtitles subtitles : subtitlesList) {
                SubtitleVO subtitlesVO = new SubtitleVO();
                BeanUtils.copyProperties(subtitles, subtitlesVO);
                subtitlesVOList.add(subtitlesVO);
            }
            solutionsVO.setSubtitlesVOList(subtitlesVOList);


            solutionsVOList.add(solutionsVO);


        }
        return solutionsVOList;

    }

    /**
     * 添加解决方案
     *
     * @param solutionsVO solutionsVO
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result addSolutions(SolutionsVO solutionsVO) {

        if (solutionsVO == null) {
            return Result.fail(EnumReturn.PARAMS_EMPTY);
        }
        if (solutionsVO.getTitle() == null || solutionsVO.getTitle().isEmpty()) {
            return Result.fail(EnumReturn.PARAMS_EMPTY);
        }

        Solutions solutions = new Solutions();
        solutions.setTitle(solutionsVO.getTitle());
        solutions.setImageUrl(solutionsVO.getUrl());
        solutions.setStates(Short.valueOf("1"));
        //solutions.setIntroduction(solutionsVO.getIntroduction());

        boolean saveSolutionResult = this.save(solutions);
        if (!saveSolutionResult) {
            return Result.fail(EnumReturn.SOLUTIONS_SAVE_ERROR);
        }

        List<SubtitleVO> subtitlesVOList = solutionsVO.getSubtitlesVOList();
        if (subtitlesVOList != null && !subtitlesVOList.isEmpty()) {
            List<Subtitles> subtitlesList = new ArrayList<>();
            for (SubtitleVO subtitleVO : subtitlesVOList) {
                Subtitles subtitle = new Subtitles();
                // BeanUtils.copyProperties(subtitleVO, subtitle);
                // 设置子标题所属的解决方案 ID
                subtitle.setSolutionId(solutions.getId());
                subtitle.setSubtitle(subtitleVO.getSubtitle());
                subtitle.setDescription(subtitleVO.getDescription());
                log.info("解决方案的id为：{}", solutions.getId());
                subtitlesList.add(subtitle);
            }
            // 批量保存子标题
            boolean saveSubtitlesResult = subtitlesMapper.insertBatch(subtitlesList);
            if (!saveSubtitlesResult) {
                // 若子标题保存失败，进行回滚
                throw new RuntimeException("子标题保存失败");
            }


        }
        return Result.success(200);
    }

    /**
     * 修改解决方案
     *
     * @param solutionsVO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateSolutions(Integer id, SolutionsVO solutionsVO) {
        // 参数校验
        if (id == null || solutionsVO == null) {
            return Result.fail(EnumReturn.PARAMS_EMPTY);
        }

        Solutions solution = this.getById(id);

        if (solution == null) {
            return Result.fail(EnumReturn.SOLUTIONS_NOT_FOUND);
        }

        if (solutionsVO.getTitle() != null) {
            solution.setTitle(solutionsVO.getTitle());
        }
        if (solutionsVO.getUrl() != null) {
            solution.setImageUrl(solutionsVO.getUrl());
        }

        if (this.updateById(solution)) {
            return Result.fail(EnumReturn.SOLUTIONS_UPDATE_ERROR);
        }

        //更新其他字段，如果搞简介的话

        // 更新子标题
        List<SubtitleVO> subtitlesVOList = solutionsVO.getSubtitlesVOList();
        if (subtitlesVOList != null) {
            // 先删除原有的子标题
            LambdaQueryWrapper<Subtitles> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(Subtitles::getSolutionId, id);
            subtitlesMapper.delete(deleteWrapper);

            // 插入新的子标题
            List<Subtitles> subtitlesList = new ArrayList<>();
            for (SubtitleVO subtitleVO : subtitlesVOList) {
                Subtitles subtitle = new Subtitles();
                //fixme 可能有bug
                BeanUtils.copyProperties(subtitleVO, subtitle);
                subtitle.setSolutionId(id);
                subtitlesList.add(subtitle);
            }
            boolean saveSubtitlesResult = subtitlesMapper.insertBatch(subtitlesList);
            if (!saveSubtitlesResult) {
                throw new RuntimeException("子标题更新失败");
            }
        }

        // 返回成功结果
        return Result.success(EnumReturn.OPERATION_SUCCESS);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteSolutions(Integer id) {
        if (null == id) {
            return Result.fail(EnumReturn.PARAMS_EMPTY);
        }

        // 检查解决方案是否存在
        Solutions solution = this.getById(id);
        if (null == solution) {
            return Result.fail(EnumReturn.SOLUTIONS_NOT_FOUND);
        }

        // 删除解决方案
        boolean deleteSolutionResult = this.removeById(id);
        if (!deleteSolutionResult) {
            return Result.fail(EnumReturn.SOLUTIONS_DELETE_ERROR);
        }

        // 删除对应的子标题
        LambdaQueryWrapper<Subtitles> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(Subtitles::getSolutionId, id);
        boolean deleteSubtitlesResult = subtitlesMapper.delete(deleteWrapper) > 0;
        if (!deleteSubtitlesResult) {
            // 可以选择抛出异常回滚事务，或者记录日志
            log.warn("删除解决方案 {} 对应的子标题时未找到相关记录", id);
        }

        return Result.success(EnumReturn.OPERATION_SUCCESS);
    }

    /**
     * 修改解决方案状态
     * @param id 解决方案ID
     * @param state 状态 1 启用 0 禁用 前端修改后给我。
     */
    @Override
    public Result changeSolutionsState(Integer id, Short state) {
        if (id == null || state == null) {
            return Result.fail(EnumReturn.PARAMS_EMPTY);
        }

        Solutions solutions = this.getById(id);
        if (solutions == null) {
            return Result.fail(EnumReturn.SOLUTIONS_NOT_FOUND);
        }

        solutions.setStates(state);

        if (this.updateById(solutions)) {
            return Result.success(EnumReturn.OPERATION_SUCCESS);
        } else {
            return Result.fail(EnumReturn.SOLUTIONS_UPDATE_ERROR);
        }
    }

}






