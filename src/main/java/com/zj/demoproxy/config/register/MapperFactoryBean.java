package com.zj.demoproxy.config.register;


import com.zj.demoproxy.template.BaseTemplate;
import com.zj.demoproxy.template.handler.BaseTemplateImpl;
import com.zj.demoproxy.template.handler.SingerInvocationHandler;
import com.zj.demoproxy.utils.ClassHelper;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.NamingStrategy;
import net.bytebuddy.description.modifier.FieldManifestation;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

public class MapperFactoryBean<T extends BaseTemplate> implements FactoryBean<T> {

    private Class<T> mapperInterface;
    @Autowired
    private ClassHelper classHelper;

    MapperFactoryBean(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    @Override
    public T getObject() {
        Class<? extends BaseTemplate> clazz = getObjectType();
        try {
            // 使用byte-buddy动态代理接口
            Object o = new ByteBuddy().with(new NamingStrategy.AbstractBase() {
                        @Override
                        protected String name(TypeDescription typeDescription) {
                            return "com.zj.demoproxy.template.service.impl." + typeDescription.getSimpleName() + "Impl";
                        }
                    })
                    .subclass(Object.class)
                    .implement(clazz)
                    // 定义一个属性private final Class typeClz; 为BaseTemplate的泛型class
                    .defineField("typeClz", Class.class, Visibility.PRIVATE, FieldManifestation.FINAL)
                    // 定义一个属性private final String dmsUrl;
                    .defineField("dmsUrl", String.class, Visibility.PRIVATE, FieldManifestation.FINAL)
                    // 覆写构造方法,赋值final属性
                    .constructor(ElementMatchers.isDefaultConstructor())
                    .intercept(MethodCall.invoke(Object.class.getDeclaredConstructor())
                            .andThen(FieldAccessor.ofField("typeClz").setsValue(ClassHelper.getBaseTemplateTypeClass(clazz)))
                            .andThen(FieldAccessor.ofField("dmsUrl").setsValue(classHelper.getDmsUrl(clazz)))
                    )
                    // 拦截get方法
                    .method(ElementMatchers.isGetter())
                    .intercept(FieldAccessor.ofBeanProperty())
                    // 通用模板方法用通用拦截
                    .method(ElementMatchers.isDeclaredBy(BaseTemplate.class))
                    .intercept(MethodDelegation.to(new BaseTemplateImpl()))
                    // 自定义方法用自定义拦截
                    .method(ElementMatchers.isDeclaredBy(clazz))
                    .intercept(InvocationHandlerAdapter.of(new SingerInvocationHandler()))
                    .make()
                    .load(ClassLoader.getSystemClassLoader())
                    .getLoaded()
                    .newInstance();
            return (T) o;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Class<? extends BaseTemplate> getObjectType() {
        return this.mapperInterface;
    }

}
