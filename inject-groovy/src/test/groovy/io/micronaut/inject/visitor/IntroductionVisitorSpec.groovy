package io.micronaut.inject.visitor

import io.micronaut.AbstractBeanDefinitionSpec
import io.micronaut.ast.groovy.TypeElementVisitorStart
import io.micronaut.inject.writer.BeanDefinitionVisitor

class IntroductionVisitorSpec extends AbstractBeanDefinitionSpec {
    def setup() {
        System.setProperty(TypeElementVisitorStart.ELEMENT_VISITORS_PROPERTY, AllElementsVisitor.name)
    }

    def cleanup() {
        AllElementsVisitor.clearVisited()
    }

    void "test that it is possible to visit introduction advice that extend from existing interfaces"() {
        buildBeanDefinition('test.MyInterface' + BeanDefinitionVisitor.PROXY_SUFFIX, '''
package test;

import io.micronaut.aop.introduction.Stub;
import io.micronaut.inject.visitor.InterfaceWithGenerics;

@Stub
interface MyInterface extends InterfaceWithGenerics<Foo, Long>  {
    String myMethod();
}

class Foo {}
''')
        expect:
        IntroductionVisitor.VISITED_CLASS_ELEMENTS.size() == 1
        IntroductionVisitor.VISITED_METHOD_ELEMENTS.size() == 2
        IntroductionVisitor.VISITED_METHOD_ELEMENTS[1].name == 'save'
        IntroductionVisitor.VISITED_METHOD_ELEMENTS[1].genericReturnType.name == 'test.Foo'
        IntroductionVisitor.VISITED_METHOD_ELEMENTS[1].parameters[0].genericType.name == 'test.Foo'
    }
}
