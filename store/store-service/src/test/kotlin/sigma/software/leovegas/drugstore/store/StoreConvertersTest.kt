package sigma.software.leovegas.drugstore.store

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import sigma.software.leovegas.drugstore.store.api.CreateStoreRequest
import sigma.software.leovegas.drugstore.store.api.StoreResponse

@DisplayName("Store converters test")
class StoreConvertersTest {

    @Test
    fun `should convert to Store`() {

        // given
        val store = Store(priceItemId = 12L,quantity = 5)
        val storeRequest = CreateStoreRequest(priceItemId = 12L, quantity = 5)

        // when
        val actual = storeRequest.toEntity()

        // then
        assertThat(actual).isEqualTo(store)
    }

    @Test
    fun `should convert to StoreResponse`() {

        // given
        val store = Store(priceItemId = 12L, quantity = 5)
        val storeResponse = StoreResponse(priceItemId = 12L, quantity = 5)

        // when
        val actual = store.toStoreResponseDTO()

        // then
        assertThat(actual).isEqualTo(storeResponse)
    }

    @Test
    fun `should convert to list of StoreResponses`() {

        // given
        val store = Store(priceItemId = 12L, quantity = 5)
        val storeResponse = StoreResponse(priceItemId = 12L, quantity = 5)
        val listOfStores = listOf(store, store)
        val expectedList = listOf(storeResponse, storeResponse)

        // when
        val actual = listOfStores.toStoreResponseList()

        // then
        assertThat(actual).isEqualTo(expectedList)
    }
}
