package org.transmart.authorization

import groovy.util.logging.Log4j
import org.springframework.aop.TargetSource
import org.springframework.aop.framework.AopInfrastructureBean
import org.springframework.aop.framework.ProxyFactory
import org.springframework.beans.BeansException
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.web.context.request.RequestContextHolder
import org.transmartproject.core.users.User

@Log4j
class CurrentUserBeanProxyFactory implements FactoryBean<User>, BeanFactoryAware {

    // Don't change this bean name. Rmodules depends on this bean name
    public final static String BEAN_BAME = 'currentUserBean'
    public final static String SUB_BEAN_REQUEST = 'currentUserBeanRequestScoped'
    public final static String SUB_BEAN_QUARTZ = 'currentUserBeanQuartzScope'

    private User object

    private List<String> extraBeansToTry = [SUB_BEAN_QUARTZ]

    void registerBeanToTry(String beanName) {
        extraBeansToTry << beanName
    }

    @Override
    User getObject() throws Exception {
        object
    }

    @Override
    Class<?> getObjectType() {
        User
    }

    @Override
    boolean isSingleton() {
        true
    }

    class CurrentUserBeanTargetSource  implements TargetSource {

        ConfigurableBeanFactory cbf

        final Class<?> targetClass = User

        final boolean isStatic() {
            false
        }

        @Override
        Object getTarget() throws Exception {
            if (RequestContextHolder.requestAttributes) {
                // request context is active
                cbf.getBean SUB_BEAN_REQUEST
            } else {
                for (beanName in extraBeansToTry) {
                    try {
                        return cbf.getBean(beanName)
                    } catch (BeansException e) {
                        log.debug("BeansException for bean ${beanName}")
                    }
                }

                throw new IllegalStateException("Tried to fetch " +
                        "current user, but it's not available")
            }
        }

        @Override
        void releaseTarget(Object target) throws Exception {
            // not really anything to do
        }
    }

    @Override
    void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        ConfigurableBeanFactory cbf = (ConfigurableBeanFactory) beanFactory;

        ProxyFactory pf = new ProxyFactory([User] as Class[])
        pf.targetSource = new CurrentUserBeanTargetSource(cbf: cbf)

        // also expose the thing as a ScopedObject
        //pf.addAdvice(new DelegatingIntroductionInterceptor(scopedObject))
        pf.addInterface(AopInfrastructureBean)

        object  = pf.getProxy(cbf.getBeanClassLoader())
    }
}
