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
    implements SolutionsService{


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
       // log.info("===== 查询到状态为 1 的解决方案列表，数量: {} =====", solutionsList.size());
       // log.debug("解决方案信息列表:{}",solutionsList);

        List<SolutionsVO> solutionsVOList = new ArrayList<>();

        //查询状态为1的解决方案的子标题
        for (Solutions solutions : solutionsList) {
            SolutionsVO solutionsVO = new SolutionsVO();

            solutionsVO.setUrl(solutions.getImageUrl());
            solutionsVO.setTitle(solutions.getTitle());
        //    log.info("----- 开始处理解决方案: {} -----", solutions.getTitle());
        //    log.debug("解决方案 VO 初始信息 :{}", solutionsVO);

            LambdaQueryWrapper<Subtitles> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1
                    .eq(Subtitles::getSolutionId, solutions.getId())
                    .eq(Subtitles::getStates, 1);
            List<Subtitles> subtitlesList = subtitlesMapper.selectList(queryWrapper1);
         //   log.info("该解决方案下状态为 1 的子标题数量: {}", subtitlesList.size());
         //   log.debug("子标题列表信息:{}",subtitlesList);

            //封装子标题到解决方案VO中
            List<SubtitleVO> subtitlesVOList = new ArrayList<>();
            for (Subtitles subtitles : subtitlesList) {
                SubtitleVO subtitlesVO = new SubtitleVO();
                BeanUtils.copyProperties(subtitles, subtitlesVO);
                subtitlesVOList.add(subtitlesVO);
            }
            solutionsVO.setSubtitlesVOList(subtitlesVOList);


            solutionsVOList.add(solutionsVO);


         //   log.debug("解决方案 VO 最终信息: {}", solutionsVO);
         //   log.info("----- 解决方案: {} 处理完成 -----", solutions.getTitle());


         //   log.info("solutionsVOList:{}", solutionsVOList);

        }
        return  solutionsVOList;

    }

    /**
     * 添加解决方案
     * @param solutionsVO
     * @return
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
        solutions.setStates(1);
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
                BeanUtils.copyProperties(subtitleVO, subtitle);
                // 设置子标题所属的解决方案 ID
                subtitle.setSolutionId(solutions.getId());
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
        return null;
    }
}




