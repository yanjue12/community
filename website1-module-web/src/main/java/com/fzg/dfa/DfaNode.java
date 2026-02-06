package com.fzg.dfa;

import java.util.HashMap;
import java.util.Map;

public class DfaNode {

    // 子节点
    private final Map<Character, DfaNode> children = new HashMap<>();

    // 是否为一个完整敏感词的结尾
    private boolean end;

    public Map<Character, DfaNode> getChildren() {
        return children;
    }

    public boolean isEnd() {
        return end;
    }

    public void setEnd(boolean end) {
        this.end = end;
    }
}
