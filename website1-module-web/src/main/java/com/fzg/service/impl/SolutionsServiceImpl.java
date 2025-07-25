package com.fzg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.config.MinioProperties;
import com.fzg.enums.EnumReturn;
import com.fzg.mapper.SubtitlesMapper;
import com.fzg.model.Result;
import com.fzg.model.Solutions;
import com.fzg.model.Subtitles;
import com.fzg.service.MinioService;
import com.fzg.service.SolutionsService;
import com.fzg.mapper.SolutionsMapper;
import com.fzg.vo.SolutionsVO;
import com.fzg.vo.SubtitleVO;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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

    @Resource
    private MinioServiceImpl minioService;

    @Resource
    private MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    /**
     * 列出所有解决方案(解决方案表 子标题表)
     */
    @Override
    public List<SolutionsVO> solutionsList() {
        LambdaQueryWrapper<Solutions> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Solutions::getStates, 1);
        List<Solutions> solutionsList = this.list(queryWrapper);

        List<SolutionsVO> solutionsVOList = new ArrayList<>();

        for (Solutions solutions : solutionsList) {
            SolutionsVO solutionsVO = new SolutionsVO();
           // BeanUtils.copyProperties(solutions, solutionsVO);
            solutionsVO.setUrl(solutions.getImageUrl());
            solutionsVO.setTitle(solutions.getTitle());
            solutionsVO.setId(solutions.getId());

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
                subtitle.setSolutionId(solutions.getId());
                subtitle.setSubtitle(subtitleVO.getSubtitle());
                subtitle.setDescription(subtitleVO.getDescription());
                subtitlesList.add(subtitle);
            }
            boolean saveSubtitlesResult = subtitlesMapper.insertBatch(subtitlesList);
            if (!saveSubtitlesResult) {
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

        String dbImageUrl = solution.getImageUrl();

        if(!dbImageUrl.equals(solutionsVO.getUrl())){
            try {
                this.minioService.removeFile(dbImageUrl, bucketName, minioClient);
            } catch (Exception e) {
                return Result.fail(EnumReturn.SOLUTIONS_UPDATE_ERROR_FOR_IMAGE);
            }

        }



        solution.setTitle(solutionsVO.getTitle());
        solution.setImageUrl(solutionsVO.getUrl());

        if(!this.updateById(solution)) {
            return  Result.fail(EnumReturn.SOLUTIONS_UPDATE_ERROR);
        }


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
                //fixme 这里其实可以搞事务传递那种更新不需要删除
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
        try {
            this.minioService.removeFile(solution.getImageUrl(), bucketName, minioClient);
        } catch (Exception e) {
            return Result.fail(EnumReturn.SOLUTIONS_DELETE_ERROR_FOR_IMAGE);
        }

        boolean deleteSolutionResult = this.removeById(id);
        if (!deleteSolutionResult) {
            return Result.fail(EnumReturn.SOLUTIONS_DELETE_ERROR);
        }

        // 删除对应的子标题
        LambdaQueryWrapper<Subtitles> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(Subtitles::getSolutionId, id);
        boolean deleteSubtitlesResult = subtitlesMapper.delete(deleteWrapper) > 0;
        if (!deleteSubtitlesResult) {
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

    @Override
    public Result AdminSolutionsList() {
        LambdaQueryWrapper<Solutions> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Solutions::getStates).orderByAsc(Solutions::getUpdatedAt);
        List<Solutions> solutionsList = this.list(queryWrapper);

        List<SolutionsVO> solutionsVOList = new ArrayList<>();

        for (Solutions solutions : solutionsList) {
            //封装解决方案到解决方案VO中
            SolutionsVO solutionsVO = new SolutionsVO();
            // BeanUtils.copyProperties(solutions, solutionsVO);
            solutionsVO.setUrl(solutions.getImageUrl());
            solutionsVO.setTitle(solutions.getTitle());
            solutionsVO.setId(solutions.getId());

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
        return Result.success(solutionsVOList);
    }

    /**
     * 批量删除解决方案
     * @param ids
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
    public Result batchDeleteSolutions(List<Integer> ids) {

        return null;
    }

}






