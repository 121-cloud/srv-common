package otocloud.common;

import io.vertx.core.shareddata.Shareable;

/**
 * 用于在Vertx实例内共享数据.
 * 比如,在同一个Vertx部署的多个Verticle中共享数据.
 * <p/>
 * zhangyef@yonyou.com on 2015-11-13.
 *
 * @see Shareable
 */
public class ShareableData implements Shareable {
    private Object data;

    public ShareableData() {
    }

    public ShareableData(Object data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
