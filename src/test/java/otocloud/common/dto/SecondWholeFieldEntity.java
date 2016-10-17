package otocloud.common.dto;

/**
 * zhangyef@yonyou.com on 2015-11-04.
 */
public class SecondWholeFieldEntity {
    int id;
    String name;
    boolean success;

    PropSub2 prop;

    public PropSub2 getProp() {
        return prop;
    }

    public void setProp(PropSub2 prop) {
        this.prop = prop;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
