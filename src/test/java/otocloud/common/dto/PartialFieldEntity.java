package otocloud.common.dto;

/**
 * zhangyef@yonyou.com on 2015-11-03.
 */
public class PartialFieldEntity {
    int id;

    @From(name = "success")
    boolean content;

    @From(clazz = FirstWholeFieldEntity.class)
    String name;

    @From(clazz = SecondWholeFieldEntity.class, name = "name", fetchPolicy = FetchPolicy.FirstNull)
    String name2;

    @From(clazz = SecondWholeFieldEntity.class, name = "name", fetchPolicy = FetchPolicy.LastNotNull)
    String name3;

    Prop prop;


    public String getName3() {
        return name3;
    }

    public void setName3(String name3) {
        this.name3 = name3;
    }

    public boolean isContent() {
        return content;
    }

    public String getName2() {
        return name2;
    }

    public void setName2(String name2) {
        this.name2 = name2;
    }

    public boolean getContent() {
        return content;
    }

    public void setContent(boolean content) {
        this.content = content;
    }

    public Prop getProp() {
        return prop;
    }

    public void setProp(Prop prop) {
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
}
