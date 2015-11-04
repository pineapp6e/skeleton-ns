
package com.hesine.hichat.access.bo;

import com.hesine.hichat.access.model.NotifyRequest;

public interface NotifyBO {
    int notifyProcess(String account, NotifyRequest notifyRequest);
}
