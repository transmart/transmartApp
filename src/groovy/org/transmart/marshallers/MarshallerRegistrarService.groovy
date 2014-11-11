package org.transmart.marshallers

import grails.converters.JSON
import groovy.util.logging.Log4j
import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanDefinitionHolder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner
import org.springframework.core.type.classreading.MetadataReader
import org.springframework.core.type.classreading.MetadataReaderFactory
import org.springframework.core.type.filter.TypeFilter

@Log4j
public class MarshallerRegistrarService implements FactoryBean {

    private final static PACKAGE = "org.transmart.marshallers"
    private final static RESOURCE_PATTERN = "**/*Marshaller.class"

    final Class objectType = null
    final boolean singleton = true

    @Autowired
    ApplicationContext ctx

    void start() {
        log.info 'Registering marshallers'

        ClassPathBeanDefinitionScanner scanner = new
                ClassPathBeanDefinitionScanner((BeanDefinitionRegistry) ctx, false) {

                    @Override
                    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
                        Set<BeanDefinitionHolder> superValue = super.doScan(basePackages)
                        log.debug "Found marshallers: $superValue"

                        superValue.each { holder ->
                            def bean = ctx.getBean(holder.beanName)
                            JSON.registerObjectMarshaller(bean.targetType,
                                    bean.&convert)
                        }

                        superValue
                    }
                }
        scanner.setResourcePattern(RESOURCE_PATTERN)
        scanner.addIncludeFilter({
            MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory ->
                metadataReader.classMetadata.className.matches(".+Marshaller")
        } as TypeFilter)

        scanner.scan(PACKAGE)
    }

    @Override
    Object getObject() throws Exception {
        start()
        null
    }
}
