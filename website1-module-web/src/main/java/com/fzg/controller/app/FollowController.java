package com.fzg.controller.app;

import com.fzg.constant.RedisFollowKey;
import com.fzg.enums.EnumReturn;
import com.fzg.model.Result;
import com.fzg.service.FollowService;
import com.fzg.vo.FollowVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/follow")
public class FollowController {

    @Autowired
    private FollowService followService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 关注
     * @return
     */
    @PostMapping("add")
    public Result addFollow(@RequestBody FollowVO follow){
        if(null == follow || follow.getFollowerId() == null || follow.getFollowingId() == null){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }
        Boolean res = followService.addFollow(follow);
        return Result.handle(res);
    }

    /**
     * 获取关注数和粉丝数
     * @param userId
     * @return
     */
    @PostMapping("getFolingAndFolCount")
    public Result getFollowingCount(Long userId) {

        Long followingCount = redisTemplate.opsForSet()
                .size(RedisFollowKey.followingSet(userId));
        Long followerCount = redisTemplate.opsForSet()
                .size(RedisFollowKey.followerSet(userId));
        Map<String, Long> map = new HashMap<>();
        map.put("followingCount", followingCount);
        map.put("followerCount", followerCount);
        return Result.success(map);
    }

}
