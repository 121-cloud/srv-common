package otocloud.common.dto;

import java.util.List;

/**
 * zhangyef@yonyou.com on 2015-11-05.
 */
public class ParticlItemListEntity {

    @From(fieldType = WholeItem.class)
    PartialItem item;

    List<PartialItem> items;

    public List<PartialItem> getItems() {
        return items;
    }

    public void setItems(List<PartialItem> items) {
        this.items = items;
    }

    public PartialItem getItem() {
        return item;
    }

    public void setItem(PartialItem item) {
        this.item = item;
    }
}
