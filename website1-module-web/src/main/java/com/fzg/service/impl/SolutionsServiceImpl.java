package com.fzg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.mapper.SubtitlesMapper;
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
}




