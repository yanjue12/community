package com.fzg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fzg.constant.RedisSolutionsKey;
import com.fzg.enums.EnumReturn;
import com.fzg.mapper.SubtitlesMapper;
import com.fzg.model.Result;
import com.fzg.model.Solutions;
import com.fzg.model.Subtitles;
import com.fzg.service.SolutionsService;
import com.fzg.mapper.SolutionsMapper;
import com.fzg.vo.SolutionsVO;
import com.fzg.vo.SubtitleVO;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
* @author yanju
* @description 针对表【solutions(solution表)】的数据库操作Service实现
* @createDate 2025-07-09 17:09:29
*/
@Service
@Slf4j
@RequiredArgsConstructor
public class SolutionsServiceImpl extends ServiceImpl<SolutionsMapper, Solutions>
    implements SolutionsService {

    private final RedisTemplate redisTemplate;

    private final SubtitlesMapper subtitlesMapper;

    private final MinioServiceImpl minioService;

    private final MinioClient minioClient;

    private final ObjectMapper objectMapper;

    @Value("${minio.bucket-name}")
    private String bucketName;

    /**
     * 列出所有解决方案(解决方案表 子标题表)
     */
    @Override
    public Page<SolutionsVO> solutionsList(Integer pageNumber, Integer pageSize) {

        String redisKey = RedisSolutionsKey.getSolutionsKey(pageNumber, pageSize);


        // 先从 Redis 中获取数据
        Object cachedData = redisTemplate.opsForValue().get(redisKey);
        if (cachedData != null) {
            try {
                return objectMapper.readValue(cachedData.toString(), Page.class);
            } catch (JsonProcessingException e) {
                log.error("反序列化 Redis 数据失败，key: {}", redisKey, e);
            }
        }

        Page<Solutions> page = new Page<>(pageNumber,pageSize);
        LambdaQueryWrapper<Solutions> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Solutions::getStates, 1);
        Page<Solutions> solutionsPage = this.page(page, queryWrapper);

        long count = 0;
        if(pageNumber == 1){
             count = this.count(queryWrapper);
             if(count % pageSize == 0){
                 count = count / pageSize;
             }else{
                 count = count / pageSize;
                 count++;
             }
        }

        Page<SolutionsVO> solutionsVOPage = new Page<>(pageNumber, pageSize, solutionsPage.getTotal());
        List<SolutionsVO> solutionsVOList = new ArrayList<>();
        List<Solutions> solutionsList = solutionsPage.getRecords();

        for (Solutions solutions : solutionsList) {
            SolutionsVO solutionsVO = new SolutionsVO();
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

        solutionsVOPage.setRecords(solutionsVOList);
        solutionsVOPage.setTotal(count);

        // 将查询结果存入 Redis，设置过期时间为 1 小时

        try {
            String jsonData = objectMapper.writeValueAsString(solutionsVOPage);
            redisTemplate.opsForValue().set(redisKey, jsonData, 1, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            log.error("序列化分页数据失败，key: {}", redisKey, e);
        }

        return solutionsVOPage;
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
                log.info("开始拷贝子标题");
                Subtitles subtitle = new Subtitles();
                // BeanUtils.copyProperties(subtitleVO, subtitle);
                subtitle.setSolutionId(solutions.getId());
                subtitle.setSubtitle(subtitleVO.getSubtitle());
                subtitle.setDescription(subtitleVO.getDescription());
                subtitlesList.add(subtitle);
                log.info("子标题拷贝完成subtitle:{}",subtitle);
            }
            boolean saveSubtitlesResult = subtitlesMapper.insertBatch(subtitlesList);
            log.info("子标题保存完成saveSubtitlesResult:{}",saveSubtitlesResult);
            if (!saveSubtitlesResult) {
                return Result.fail(EnumReturn.SOLUTIONS_SUBTITLES_SAVE_ERROR);
            }
        }

        //删cache
        clearSolutionsCache();
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

        clearSolutionsCache();
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

        clearSolutionsCache();
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
            clearSolutionsCache();
            return Result.success(EnumReturn.OPERATION_SUCCESS);
        } else {
            return Result.fail(EnumReturn.SOLUTIONS_UPDATE_ERROR);
        }

    }



    /**
     * 清除所有解决方案相关的Redis缓存
     */
    private void clearSolutionsCache() {
        try {
            // 匹配所有solutions分页缓存键（假设键格式为"solutions:page:*:size:*"）
            Set<String> keys = redisTemplate.keys("solutions:page:*:size:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("清除Solutions缓存成功，共删除 {} 个缓存项", keys.size());
            } else {
                log.info("没有找到需要清除的Solutions缓存");
            }
        } catch (Exception e) {
            log.error("清除Solutions缓存失败", e);
        }
    }






    @Override
    public Result<List<SolutionsVO>> AdminSolutionsList() {

        LambdaQueryWrapper<Solutions> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Solutions::getStates).orderByAsc(Solutions::getUpdatedAt);



        List<SolutionsVO> solutionsVOList = new ArrayList<>();

        List<Solutions> solutionsList = this.list(queryWrapper);

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






