import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.OngoingStubbing
import kotlin.reflect.KClass

internal fun <T : Any> mock(clazz: KClass<T>): T = Mockito.mock(clazz.java)

internal fun <T> callOf(methodCall: T): OngoingStubbing<T> = Mockito.`when`(methodCall)

internal infix fun <T> OngoingStubbing<T>.willReturn(value: T): OngoingStubbing<T> = thenReturn(value)

internal infix fun <T> OngoingStubbing<T>.willDo(f: (InvocationOnMock) -> T): OngoingStubbing<T> = then(f)