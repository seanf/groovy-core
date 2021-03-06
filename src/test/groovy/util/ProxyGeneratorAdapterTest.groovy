package groovy.util

import org.codehaus.groovy.runtime.ProxyGeneratorAdapter

class ProxyGeneratorAdapterTest extends GroovyTestCase {
    void testShouldCreateProxy() {
        def map = ['toString': { 'HELLO' }]
        ProxyGeneratorAdapter adapter = new ProxyGeneratorAdapter(map, Object, null, this.class.classLoader, false, null)
        def obj = adapter.proxy(map)
        assert obj instanceof GroovyObject
        assert obj.toString() == 'HELLO'
    }

    void testImplementSingleAbstractMethod() {
        def map = ['m': { 'HELLO' }]
        ProxyGeneratorAdapter adapter = new ProxyGeneratorAdapter(map, Foo, null, this.class.classLoader, false, null)
        def obj = adapter.proxy(map)
        assert obj instanceof GroovyObject
        assert obj instanceof Foo
        assert obj.m() == 'HELLO'

    }
    
    void testImplementSingleAbstractMethodReturningVoid() {
        def map = ['bar': { println 'HELLO' }]
        ProxyGeneratorAdapter adapter = new ProxyGeneratorAdapter(map, Bar, null, this.class.classLoader, false, null)
        def obj = adapter.proxy(map)
        assert obj instanceof GroovyObject
        assert obj instanceof Bar
        obj.bar()

    }

    void testImplementSingleAbstractMethodReturningVoidAndSharedVariable() {
        def x = null
        def map = ['bar': { x = 'HELLO' }]
        ProxyGeneratorAdapter adapter = new ProxyGeneratorAdapter(map, Bar, null, this.class.classLoader, false, null)
        def obj = adapter.proxy(map)
        assert obj instanceof GroovyObject
        assert obj instanceof Bar
        assert x == null
        obj.bar()
        assert x == 'HELLO'
    }

    void testImplementMethodFromInterface() {
        def map = ['foo': { 'HELLO' }]
        ProxyGeneratorAdapter adapter = new ProxyGeneratorAdapter(map, Object, [FooInterface] as Class[], this.class.classLoader, false, null)
        def obj = adapter.proxy(map)
        assert obj instanceof GroovyObject
        assert obj instanceof FooInterface
        assert obj.foo() == 'HELLO'
    }

    void testImplementMethodFromInterfaceUsingInterfaceAsSuperClass() {
        def map = ['foo': { 'HELLO' }]
        ProxyGeneratorAdapter adapter = new ProxyGeneratorAdapter(map, FooInterface, null, this.class.classLoader, false, null)
        def obj = adapter.proxy(map)
        assert obj instanceof GroovyObject
        assert obj instanceof FooInterface
        assert obj.foo() == 'HELLO'
    }

    void testImplementMethodFromInterfaceAndSuperClass() {
        def x = null
        def map = ['foo': { 'HELLO' }, 'bar': { x='WORLD'} ]
        ProxyGeneratorAdapter adapter = new ProxyGeneratorAdapter(map, Bar, [FooInterface] as Class[], this.class.classLoader, false, null)
        def obj = adapter.proxy(map)
        assert obj instanceof GroovyObject
        assert obj instanceof Bar
        assert obj instanceof FooInterface
        assert x == null
        assert obj.foo() == 'HELLO'
        obj.bar()
        assert x == 'WORLD'
    }
    
    void testImplementMethodFromInterfaceWithPrimitiveTypes() {
        def map = ['calc': { x -> x*2 } ]
        ProxyGeneratorAdapter adapter = new ProxyGeneratorAdapter(map, Bar, [OtherInterface] as Class[], this.class.classLoader, false, null)
        def obj = adapter.proxy(map)
        assert obj instanceof GroovyObject
        assert obj instanceof OtherInterface
        assert obj.calc(3) == 6
    }
    
    void testWildcardProxy() {
        def map = ['*': { '1' } ]
        ProxyGeneratorAdapter adapter = new ProxyGeneratorAdapter(map, Foo, null, this.class.classLoader, false, null)
        def obj = adapter.proxy(map)
        assert obj instanceof GroovyObject
        assert obj instanceof Foo
        assert obj.m() == '1'
    }

    void testDelegatingProxy() {
        assertScript '''
        public abstract class A { abstract protected String doIt() }

        class B extends A {
           String doIt() { 'foo' }
        }
        def map = [ x : { int a, int b -> } ]
        def adapter = new org.codehaus.groovy.runtime.ProxyGeneratorAdapter(map, B, null, B.classLoader, false, B)
        def pxy = adapter.delegatingProxy(new B(), map)
        assert pxy.doIt() ==  'foo'
        '''
    }

    abstract static class Foo {
        abstract String m()
    }

    abstract static class Bar {
        abstract void bar()
    }
    
    static interface FooInterface {
        String foo()
    }
    
    static interface OtherInterface {
        int calc(int x)
    }
}
