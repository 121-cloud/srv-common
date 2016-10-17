package otocloud.common.dto;

/**
 * zhangyef@yonyou.com on 2015-11-03.
 */
public class FirstWholeFieldEntity {
    int id;
    String name;
    String nickName;

    PropSub1 prop;

    public PropSub1 getProp() {
        return prop;
    }

    public void setProp(PropSub1 prop) {
        this.prop = prop;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    boolean flag;

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

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }
}
