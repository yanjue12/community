package com.fzg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fzg.model.NewsDetail;
import com.fzg.model.Result;
import com.fzg.vo.NewsDetailsVO;

/**
* @author fzg
* @description 针对表【news_detail】的数据库操作Service
 * @createDate 2025-07-14 13:31:10
*/
public interface NewsDetailService extends IService<NewsDetail> {
    Result<NewsDetailsVO> selectByNewsId(Integer id);
}
