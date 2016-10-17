package otocloud.common.dto;

import org.junit.Assert;
import org.junit.Test;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * ObjectConvertor Tester.
 *
 * @author zhangye
 * @version 1.0
 * @since <pre>十一月 3, 2015</pre>
 */
public class ObjectConvertorTest {

    /**
     * Method: toDTO(Class<T> dto, Object... objects)
     */
    @Test
    public void testToDTO() throws Exception {
        FirstWholeFieldEntity wholeFieldEntity = new FirstWholeFieldEntity();
        wholeFieldEntity.setId(1);
        wholeFieldEntity.setName("from whole");
        wholeFieldEntity.setNickName("from whole nickName");
        wholeFieldEntity.setFlag(true);

        PartialFieldEntity partialFieldEntity = ObjectConvertor.toDTO(PartialFieldEntity.class, wholeFieldEntity);
        Assert.assertEquals(1, partialFieldEntity.getId());
        Assert.assertEquals("from whole", partialFieldEntity.getName());

    }

    @Test
    public void it_should_be_merged_from_two_classes() {
        FirstWholeFieldEntity first = new FirstWholeFieldEntity();
        first.setId(1);
        first.setName("from whole");
        first.setNickName("from whole nickName");
        first.setFlag(false);
        first.setProp(new PropSub1());

        SecondWholeFieldEntity second = new SecondWholeFieldEntity();
        second.setId(2);
        second.setName("from second");
        second.setSuccess(true);
        second.setProp(new PropSub2());

        SecondWholeFieldEntity secondOther = new SecondWholeFieldEntity();
        secondOther.setId(3);
        secondOther.setName(null); //set to null
        secondOther.setSuccess(false);
        secondOther.setProp(new PropSub2());

        PartialFieldEntity partialFieldEntity = ObjectConvertor.toDTO(PartialFieldEntity.class,
                first, second);
        Assert.assertEquals(1, partialFieldEntity.getId());
        Assert.assertEquals(PropSub1.class, partialFieldEntity.getProp().getClass());
        Assert.assertTrue(partialFieldEntity.getContent());
        Assert.assertEquals("from second", partialFieldEntity.getName2());
    }

    @Test
    public void it_should_be_merged_from_same_classes() {
        SecondWholeFieldEntity second = new SecondWholeFieldEntity();
        second.setId(2);
        second.setName("from second");
        second.setSuccess(false);
        second.setProp(new PropSub2());

        SecondWholeFieldEntity secondOther = new SecondWholeFieldEntity();
        secondOther.setId(3);
        secondOther.setName(null); //set to null
        secondOther.setSuccess(true);
        secondOther.setProp(new PropSub2());

        PartialFieldEntity partialFieldEntity = ObjectConvertor.toDTO(PartialFieldEntity.class,
                second, secondOther);
        Assert.assertEquals(2, partialFieldEntity.getId());
        Assert.assertEquals(PropSub2.class, partialFieldEntity.getProp().getClass());
        Assert.assertFalse(partialFieldEntity.getContent());
        Assert.assertEquals(null, partialFieldEntity.getName2());
        Assert.assertEquals("from second", partialFieldEntity.getName3());
    }

    @Test
    public void it_should_be_converted_from_object_field() {
        WholeItem wholeItem3 = new WholeItem();
        wholeItem3.setId(3);
        wholeItem3.setItemName("wholeName3");

        WholeItemListEntity wholeItemListEntity = new WholeItemListEntity();
        wholeItemListEntity.setItem(wholeItem3);

        ParticlItemListEntity entity = ObjectConvertor.toDTO(ParticlItemListEntity.class, wholeItemListEntity);

        Assert.assertEquals("wholeName3", entity.getItem().getItemName());
    }

    @Test
    public void it_should_be_merged_from_a_set_field() {
        WholeItem wholeItem = new WholeItem();
        wholeItem.setId(0);
        wholeItem.setItemName("wholeName");

        WholeItem wholeItem2 = new WholeItem();
        wholeItem2.setId(2);
        wholeItem2.setItemName("wholeName2");


        List<WholeItem> wholeItems = new LinkedList<>();
        wholeItems.add(wholeItem);
        wholeItems.add(wholeItem2);

        WholeItemListEntity wholeItemListEntity = new WholeItemListEntity();
        wholeItemListEntity.setItems(wholeItems);

        ParticlItemListEntity entity = ObjectConvertor.toDTO(ParticlItemListEntity.class, wholeItemListEntity);
        Assert.assertEquals(2, entity.getItems().size());

        PartialItem partialItem = entity.getItems().get(0);
        Assert.assertEquals("wholeName", partialItem.getItemName());


    }

    @Test
    public void testParaType() {

        Field[] fields = PartialFieldEntity.class.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            Type type = field.getGenericType();
            if (type instanceof ParameterizedTypeImpl) {
                ParameterizedTypeImpl paraType = (ParameterizedTypeImpl) type;
                //如果是集合类
                if (Collection.class.isAssignableFrom(paraType.getRawType())) {
                    System.out.println("Is a collection.");
                    //获得泛型实际参数(集合的元素类型)
                    Type[] actualTypes = paraType.getActualTypeArguments();
                    //集合类的泛型只有一个
                    Class actualType = (Class) actualTypes[0];
                }
            }
            System.out.println(fields[i]);
        }
    }

} 
