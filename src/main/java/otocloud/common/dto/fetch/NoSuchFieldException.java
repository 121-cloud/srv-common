package otocloud.common.dto.fetch;

/**
 * zhangyef@yonyou.com on 2015-11-03.
 */
public class NoSuchFieldException extends RuntimeException {
    public NoSuchFieldException(){
        super();
    }

    public NoSuchFieldException(String message){
        super(message);
    }
}
