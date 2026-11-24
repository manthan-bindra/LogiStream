package com.logistream.service;

import com.logistream.util.LogiStreamUtil;
import com.logistream.util.LogiStreamUtil; import java.util.ArrayList; import java.util.List;
/** Keeps an in-memory action history for the current run; file logging is postponed to Final Evaluation. */
public class AuditLogger { private final List<String> entries=new ArrayList<String>();
 public synchronized void log(String action){entries.add(LogiStreamUtil.nowText()+" | "+action);}
 public synchronized List<String> readLast(int n){return new ArrayList<String>(entries.subList(Math.max(0,entries.size()-n),entries.size()));}
}
