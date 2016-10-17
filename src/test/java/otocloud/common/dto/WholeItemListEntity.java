package otocloud.common.dto;

import java.util.List;

/**
 * zhangyef@yonyou.com on 2015-11-05.
 */
public class WholeItemListEntity {
    WholeItem item;

    List<WholeItem> items;

    public List<WholeItem> getItems() {
        return items;
    }

    public void setItems(List<WholeItem> items) {
        this.items = items;
    }

    public WholeItem getItem() {
        return item;
    }

    public void setItem(WholeItem item) {
        this.item = item;
    }
}
