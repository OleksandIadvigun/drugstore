package sigma.software.leovegas.drugstore.store

import io.restassured.RestAssured
import io.restassured.builder.RequestSpecBuilder
import io.restassured.specification.RequestSpecification
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.restassured3.RestAssuredRestDocumentation
import org.springframework.test.context.junit.jupiter.SpringExtension

@AutoConfigureRestDocs
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class, RestDocumentationExtension::class)
class RestApiDocumentationTest {

    lateinit var documentationSpec: RequestSpecification

    @BeforeEach
    fun setUp(restDocumentation: RestDocumentationContextProvider?) {
        documentationSpec = RequestSpecBuilder()
            .addFilter(RestAssuredRestDocumentation.documentationConfiguration(restDocumentation))
            .build()
    }

    fun of(snippet: String): RequestSpecification {
        val processor = Preprocessors.modifyUris().scheme("http").host("localhost").port(8080)
        val prettyPrint = Preprocessors.prettyPrint()
        val requestProcessor = Preprocessors.preprocessRequest(
            prettyPrint,
            processor
        )
        val responseProcessor = Preprocessors.preprocessResponse(
            prettyPrint,
            processor
        )
        return RestAssured.given(documentationSpec).filter(
            RestAssuredRestDocumentation.document(
                snippet, requestProcessor, responseProcessor
            )
        )
    }
}
