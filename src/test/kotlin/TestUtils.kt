import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.OngoingStubbing
import kotlin.reflect.KClass

fun <T : Any> mock(clazz: KClass<T>): T = Mockito.mock(clazz.java)

fun <T> callOf(methodCall: T): OngoingStubbing<T> = Mockito.`when`(methodCall)

infix fun <T> OngoingStubbing<T>.willReturn(value: T): OngoingStubbing<T> = thenReturn(value)

infix fun <T> OngoingStubbing<T>.willDo(f: (InvocationOnMock) -> T): OngoingStubbing<T> = then(f)