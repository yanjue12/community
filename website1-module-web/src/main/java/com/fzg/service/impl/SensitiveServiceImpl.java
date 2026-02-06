package com.fzg.service.impl;

import com.fzg.service.SensitiveService;
import org.springframework.stereotype.Service;

import java.util.List;

//@Service
//public class SensitiveServiceImpl implements SensitiveService {
//
//    private static final List<String> WORDS = List.of("赌博", "黄色", "政治");
//
//    @Override
//    public String hit(String... texts) {
//        for (String text : texts) {
//            if (text == null) continue;
//            for (String w : WORDS) {
//                if (text.contains(w)) {
//                    return w;
//                }
//            }
//        }
//        return null;
//    }
//}
