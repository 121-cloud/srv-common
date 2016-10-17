package otocloud.common.dto;

/**
 * zhangyef@yonyou.com on 2015-11-05.
 */
public class PartialItem {

    @From(clazz = WholeItem.class)
    String itemName;

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
}
